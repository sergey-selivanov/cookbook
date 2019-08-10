package org.sergeys.cookbook.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.h2.tools.RunScript;

public final class Database {
    private static final String FILENAME = "cookbook";

    private static Object instanceLock = new Object();
    private static Database instance;

    // singleton
    private Database() throws Exception
    {
        upgradeOrCreateIfNeeded();
    }

    public static Database getInstance() throws Exception
    {
        synchronized (instanceLock) {
            if(instance == null){
                instance = new Database();
            }
        }

        return instance;
    }

    private Connection connection;

    //@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="DMI_CONSTANT_DB_PASSWORD", justification="I know what I'm doing")
    protected Connection getConnection() throws SQLException
    {
        if(connection == null || connection.isClosed()){
            // http://www.h2database.com/html/features.html#other_logging
            // log to slf4j
            String url = String.format("jdbc:h2:%s/%s;TRACE_LEVEL_FILE=4", Settings.getSettingsDirPath(), Database.FILENAME).replace('\\', '/');
            //String url = String.format("jdbc:h2:%s/%s", Settings.getSettingsDirPath(), Database.FILENAME).replace('\\', '/');
            //String url = String.format("jdbc:h2:%s/%s;JMX=TRUE", Settings.getSettingsDirPath(), Database.FILENAME).replace('\\', '/');
            connection = DriverManager.getConnection(url, "sa", "sa");
        }

        return connection;
    }

    private void upgrade() throws SQLException, IOException
    {
        Statement st = null;
        ResultSet rs = null;
        InputStream in = null;
        try {
            st = getConnection().createStatement();
            rs = st.executeQuery("select val from properties where property='version'");
            rs.next();
            String version = rs.getString("val");
            //rs.close();
            int ver = Integer.valueOf(version);

            // apply all existing upgrades
            in = getClass().getResourceAsStream("/upgrade" + ver + ".sql");
            while(in != null){
                RunScript.execute(getConnection(), new InputStreamReader(in, Charset.defaultCharset()));
                in.close();
                Settings.getLogger().info("Upgraded database from version " + ver);
                ver++;
                in = getClass().getResourceAsStream("/upgrade" + ver + ".sql");
            }

            //st.close();
        } catch (SQLException | IOException e) {
            Settings.getLogger().error("failed to upgrade db", e);
        }
        finally{
            if(in != null){
                in.close();
            }
            if(rs != null){
                rs.close();
            }
            if(st != null){
                st.close();
            }
        }
    }

    private void upgradeOrCreateIfNeeded() throws Exception {

        File dir = new File(Settings.getSettingsDirPath());
        if(!dir.exists()){
            if(!dir.mkdirs()){
                throw new Exception("Failed to create settings dir at " + Settings.getSettingsDirPath());
            }
        }

        Connection conn = getConnection();

        try {
            // check whether table Properties exist
            ResultSet rs = conn.getMetaData().getTables(conn.getCatalog(), "PUBLIC", "PROPERTIES", null); // table names are uppercase

            if(!rs.next()){
                // create new structure
                InputStream in = Database.class.getResourceAsStream("/createdb.sql");
                RunScript.execute(conn, new InputStreamReader(in, Charset.defaultCharset()));
            }

            // apply upgrades
            upgrade();

        } catch (SQLException | IOException e) {
            throw e;
        }
        finally{
            conn.close();
        }
    }

    public boolean isRecipeExists(String hash){
        try {
            PreparedStatement pst = getConnection().prepareStatement("select id from recipes where hash = ?");
            pst.setString(1, hash);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                return true;
            }
        } catch (SQLException e) {
            Settings.getLogger().error("", e);
        }

        return false;
    }

    public long addRecipe(String hash, File jarfile, String title, String originalfile){

        long id = 0;
        PreparedStatement pst = null;
        InputStream is = null;
        try {
             pst = getConnection().prepareStatement(
                    "insert into recipes (hash, title, packedfile, filesize, dateadded, originalfilename) " +
                    "values (?, ?, ?, ?, ?, ?)");
            pst.setString(1, hash);
            pst.setString(2, title);
            is = new FileInputStream(jarfile);
            pst.setBinaryStream(3, is);
            pst.setLong(4, jarfile.length());
            pst.setLong(5, new Date().getTime());
            pst.setString(6, originalfile);

            pst.executeUpdate();

            is.close();

            ResultSet rs = pst.getGeneratedKeys();
//ResultSetMetaData meta = rs.getMetaData();
//int count = meta.getColumnCount();
//while(rs.next()){
//System.out.println("inserted " + rs.getLong(1));
//}

            if(rs.next()){
                id = rs.getLong(1);
            }

            pst.close();
        } catch (SQLException | IOException e) {
            Settings.getLogger().error("failed to add recipe", e);
        }
//        finally{
//            try {
//                //is.close();	// findbugs says it will be null here
//                //pst.close();
//            } catch (SQLException | IOException e) {
//                Settings.getLogger().error("", e);
//            }
//        }

        return id;
    }

    public ArrayList<Recipe> getAllRecipes(){
        ArrayList<Recipe> recipes = new ArrayList<Recipe>();
        try {
            //PreparedStatement pst = getConnection().prepareStatement("select hash, title from recipes");
            Statement st = getConnection().createStatement();
            ResultSet rs = st.executeQuery("select id, hash, title from recipes");
            while(rs.next()){
                Recipe r = new Recipe();
                r.setId(rs.getLong("id"));
                r.setHash(rs.getString("hash"));
                r.setTitle(rs.getString("title"));
                recipes.add(r);
            }

            st.close();
        } catch (SQLException e) {
            Settings.getLogger().error("", e);
        }

        return recipes;
    }

    public void extractRecipeFile(String hash, File targetFile){
        try {
            PreparedStatement pst = getConnection().prepareStatement("select packedfile from recipes where hash = ?");
            pst.setString(1, hash);
            ResultSet rs = pst.executeQuery();
            while(rs.next()){
                InputStream is = rs.getBinaryStream("packedfile");

                FileOutputStream fos = new FileOutputStream(targetFile);
                byte[] buf = new byte[20480];
                int count = is.read(buf);
                while(count > 0){
                    fos.write(buf, 0, count);
                    count = is.read(buf);
                }

                fos.close();
                is.close();

//				ReadableByteChannel ich = Channels.newChannel(is);
//				FileOutputStream fos = new FileOutputStream(targetFile);
//
//				// magic number for Windows, 64Mb - 32Kb)
//				int maxCount = (64 * 1024 * 1024) - (32 * 1024);
//
//				int available = is.available();	// returns zero here
//				long position = 0;
//				while(available > 0){
//					int count = (available > maxCount) ? maxCount: available;
//					position += fos.getChannel().transferFrom(ich, position, count);
//					available = is.available();
//				}
//
//				fos.close();
//				ich.close();
//				is.close();
            }
        } catch (SQLException | IOException e) {
            Settings.getLogger().error("", e);
        }
    }

    public ArrayList<Tag> getRootTags(){
        ArrayList<Tag> tags = new ArrayList<Tag>();

        try {
            Statement st = getConnection().createStatement();
            ResultSet rs = st.executeQuery(
                    "select id, parentid, val, specialid" +
                    " from tags where parentid is null" +
                    " order by displayorder, val");
            while(rs.next()){
                Tag t = new Tag();
                t.setId(rs.getLong("id"));
                t.setParentid(rs.getLong("parentid"));
                t.setVal(rs.getString("val"));
                int i = rs.getInt("specialid");	// 0 if null
                t.setSpecialid(i);
                tags.add(t);
            }

            st.close();
        } catch (SQLException e) {
            Settings.getLogger().error("", e);
        }

        return tags;
    }

    public ArrayList<Tag> getChildrenTags(String tag){
        ArrayList<Tag> tags = new ArrayList<Tag>();

        try {
            String sql =
"select child.id, child.parentid, child.val" +
" from tags t" +
" left join tags child on child.parentid = t.id" +
" where child.parentid is not null and t.val = ?";

            PreparedStatement pst = getConnection().prepareStatement(sql);
            pst.setString(1, tag);
            ResultSet rs = pst.executeQuery();
            while(rs.next()){
                Tag t = new Tag();
                t.setId(rs.getLong("id"));
                t.setParentid(rs.getLong("parentid"));
                t.setVal(rs.getString("val"));
                tags.add(t);
            }

            pst.close();
        } catch (SQLException e) {
            Settings.getLogger().error("", e);
        }

        return tags;
    }

    public ArrayList<Recipe> getRecipesWithoutTags(){
        ArrayList<Recipe> recipes = new ArrayList<>();

        Statement st;
        try {
            st = getConnection().createStatement();
            ResultSet rs = st.executeQuery(
                    "select hash, title from recipes r" +
                    " left join recipetags rt on rt.recipeid = r.id" +
                    " where rt.tagid is null order by dateadded");

            while(rs.next()){
                Recipe r = new Recipe();
                r.setHash(rs.getString("hash"));
                r.setTitle(rs.getString("title"));
                recipes.add(r);
            }

            st.close();
        } catch (SQLException e) {
            Settings.getLogger().error("", e);
        }


        return recipes;
    }

    public ArrayList<Recipe> getRecipesByTag(String tag){
        ArrayList<Recipe> recipes = new ArrayList<>();

        try {
            PreparedStatement pst = getConnection().prepareStatement(
                    "select hash, title from recipes r" +
                    " left join recipetags rt on rt.recipeid = r.id" +
                    " left join tags t on t.id = rt.tagid" +
                    " where t.val = ? order by dateadded");
            pst.setString(1, tag);
            ResultSet rs = pst.executeQuery();
            while(rs.next()){
                Recipe r = new Recipe();
                r.setHash(rs.getString("hash"));
                r.setTitle(rs.getString("title"));
                recipes.add(r);
            }

            pst.close();
        } catch (SQLException e) {
            Settings.getLogger().error("", e);
        }

        return recipes;
    }

    /**
     * Create tags that are missing from given list
     */
    private void validateTags(List<String> tags){
        try {
            PreparedStatement pstCheck = getConnection().prepareStatement("select id from tags where val = ?");
            PreparedStatement pstAdd = getConnection().prepareStatement("insert into tags (val, displayorder) values (?, ?)");

            getConnection().setAutoCommit(false);

            //int i = 1;
            for(String tag: tags){
                pstCheck.setString(1, tag);
                ResultSet rs = pstCheck.executeQuery();
                if(!rs.next()){
                    pstAdd.setString(1, tag);
                    pstAdd.setInt(2, 1);
                    pstAdd.addBatch();
                }
            }

            pstAdd.executeBatch();
            getConnection().commit();

            pstAdd.close();
            pstCheck.close();

            getConnection().setAutoCommit(true);

        } catch (SQLException e) {
            Settings.getLogger().error("", e);
        }
    }

    public void updateRecipeTags(String hash, List<String> tags){
        validateTags(tags);

        try {
            PreparedStatement pst = getConnection().prepareStatement("select id from recipes where hash = ?");
            pst.setString(1, hash);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                long id = rs.getLong("id");

                PreparedStatement pstUpd = getConnection().prepareStatement("delete from recipetags where recipeid = ?");
                pstUpd.setLong(1, id);
                pstUpd.executeUpdate();

                pstUpd = getConnection().prepareStatement("insert into recipetags (recipeid, tagid) values (?, (select id from tags where val = ?))");
                pstUpd.setLong(1, id);
                for(String tag: tags){
                    pstUpd.setString(2, tag);
                    pstUpd.addBatch();
                }
                pstUpd.executeBatch();

                pstUpd.close();
            }

            pst.close();
        } catch (SQLException e) {
            Settings.getLogger().error("", e);
        }
    }

    public ArrayList<String> getRecipeTags(String hash){
        ArrayList<String> tags = new ArrayList<>();

        try {
            PreparedStatement pst = getConnection().prepareStatement(
                    "select val from tags t" +
                    " left join recipetags rt on rt.tagid = t.id" +
                    " left join recipes r on r.id = rt.recipeid" +
                    " where r.hash = ?" +
                    " order by val");
            pst.setString(1, hash);
            ResultSet rs = pst.executeQuery();
            while(rs.next()){
                tags.add(rs.getString("val"));
            }

            pst.close();
        } catch (SQLException e) {
            Settings.getLogger().error("", e);
        }

        return tags;
    }

    public void updateRecipe(String hash, String newTitle){
        try {
            PreparedStatement pst = getConnection().prepareStatement(
                    "update recipes set title = ? where hash = ?");
            pst.setString(1, newTitle);
            pst.setString(2, hash);
            pst.executeUpdate();
            pst.close();
        } catch (SQLException e) {
            Settings.getLogger().error("", e);
        }
    }
}
