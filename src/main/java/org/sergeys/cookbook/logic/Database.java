package org.sergeys.cookbook.logic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.output.ValidateOutput;
import org.flywaydb.core.api.output.ValidateResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Database {
    private static final String FILENAME = "cookbook";
    private static final String LOGIN = "sa";
    private static final String PASSWD = "sa";

    //private static Object instanceLock = new Object();
    //private static Database instance;

    // TODO static vs nonstatic Logger?
    private final static Logger log = LoggerFactory.getLogger(Database.class);

    private Connection connection;

    private static final String connectionUrl = String.format("jdbc:h2:%s/%s;TRACE_LEVEL_FILE=4",
            SettingsManager.getInstance().getDataDirPath(), Database.FILENAME).replace('\\', '/');

    public Database()
    {
        //upgradeOrCreateIfNeeded();
    }

//    public static Database getInstance() throws Exception
//    {
//        synchronized (instanceLock) {
//            if(instance == null){
//                instance = new Database();
//            }
//        }
//
//        return instance;
//    }

    /**
     *	Validate and migrate with flywaydb, create if does not exist
     *
     * @throws CookbookException
     */
    public static void validate() throws CookbookException {

        // TODO use DataSource?
        final FluentConfiguration config = Flyway.configure()
                .dataSource(connectionUrl, LOGIN, PASSWD)
                //.locations("classpath:org/sergeys/cookbook/logic")
                .installedBy("cookbook")
                .baselineVersion("1.0.0");

//        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//        //ClassLoader classLoader1 = CookBook.class.getClassLoader();
//
//        ClassicConfiguration config1 = new ClassicConfiguration(classLoader);
//        config1.setDataSource(connectionUrl, LOGIN, PASSWD);

        final Flyway flyway = config.load();
        //Flyway flyway = new Flyway(config);

        if(flyway.info().all().length == 0) {
            throw new CookbookException("no flywaydb migration files found");
        }

        String baseFilename = SettingsManager.getInstance().getDataDirPath() + File.separator + FILENAME;

        // check if db file exists
        if(Files.exists(Path.of(baseFilename + ".mv.db"))
                || Files.exists(Path.of(baseFilename + ".h2.db"))) {

            // check if old database needs baselining
            ValidateResult validateResult = flyway.validateWithResult();
            if(!validateResult.validationSuccessful) {
                log.error(validateResult.errorDetails.errorMessage);

                if(!validateResult.invalidMigrations.isEmpty()) {
                    ValidateOutput validateOutput = validateResult.invalidMigrations.get(0);

                    if(validateOutput.version.equals("1.0.0")
                            && validateOutput.errorDetails.errorCode == ErrorCode.RESOLVED_VERSIONED_MIGRATION_NOT_APPLIED) {
                        // migration 1.0.0 was not applied

                        Set<String> tables = new HashSet<>();

                        try(Connection connection = DriverManager.getConnection(connectionUrl, LOGIN, PASSWD);
                                Statement st = connection.createStatement();
                                ResultSet rs = st.executeQuery("show tables")
                                ){
                            while(rs.next()){
                                log.debug(rs.getString("table_name"));
                                tables.add(rs.getString("table_name"));
                            }
                        } catch (SQLException ex) {
                            throw new CookbookException("failed", ex);
                        }

                        if(tables.isEmpty()) {
                            log.info("schema is empty, migrate");
                            //MigrateResult result = flyway.migrate(); // will throw on error
                            //flywayMigrate();
                            flyway.migrate();
                            return;
                        }
                        else {
                            //tables.containsAll()
                            tables.removeAll(Set.of("PROPERTIES", "RECIPES", "RECIPETAGS", "TAGS"));

                            if(tables.isEmpty()) {
                                log.info("existing db looks good, baseline");
                                //flyway = config.baselineVersion("1.0.0").load();
                                flyway.baseline();
                                return;
                            }
                            else {
                                throw new CookbookException("existing database has extra tables");
                            }
                        }

                    }
                    else {
                        // 1st invalid migration is not 1.0.0
                        throw new CookbookException(validateResult.errorDetails.errorMessage);
                    }

                }
                else {
                    // validation not successful but no invalid migrations
                    throw new CookbookException(validateResult.errorDetails.errorMessage);
                }
            }
            else {
                log.debug("db validation successful");
            }
        }
        else {
            // no database file yet, just create
            log.debug("no database file yet");
            //flywayMigrate();
            flyway.migrate();
        }

    }

/*
    private static void flywayMigrate() throws CookbookException {

// migrations are not visible after jlink
// https://stackoverflow.com/questions/59902719/flyway-is-not-able-to-find-migrations-in-classpath-only-if-i-run-application-aft

        log.debug("migrate");

        // TODO use DataSource?
        FluentConfiguration config = Flyway.configure()
                .dataSource(connectionUrl, LOGIN, PASSWD)
//                .locations("classpath:db/migration",	// default
//                        "classpath:cookbook/db/migration")	// after jlink?
                .installedBy("cookbook");

        Flyway flyway = config.load();

        if(flyway.info().all().length == 0) {
            throw new CookbookException("no migration files found");
        }

        //MigrateResult result = flyway.migrate(); // will throw on error
        flyway.migrate(); // will throw on error
    }
*/
    public void close() throws SQLException {
        if(connection != null) {
            connection.close();
        }
    }

//    public static Database getInstance() throws SQLException {
//        throw new SQLException("not supported");
//    }


    //@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="DMI_CONSTANT_DB_PASSWORD", justification="I know what I'm doing")
    protected Connection getConnection() throws SQLException
    {
        if(connection == null || connection.isClosed()){
            // http://www.h2database.com/html/features.html#other_logging
            // log to slf4j
 //           String url = String.format("jdbc:h2:%s/%s;TRACE_LEVEL_FILE=4", Settings.getDataDirPath(), Database.FILENAME).replace('\\', '/');
            //String url = String.format("jdbc:h2:%s/%s", Settings.getSettingsDirPath(), Database.FILENAME).replace('\\', '/');
            //String url = String.format("jdbc:h2:%s/%s;JMX=TRUE", Settings.getSettingsDirPath(), Database.FILENAME).replace('\\', '/');
            connection = DriverManager.getConnection(connectionUrl, LOGIN, PASSWD);
        }

        return connection;
    }

    public boolean isRecipeExists(String hash){

        try(PreparedStatement pst = getConnection().prepareStatement("select id from recipes where hash = ?")){
            pst.setString(1, hash);
            try(ResultSet rs = pst.executeQuery()){
                return rs.next();
            }
        } catch (SQLException ex) {
            log.error("", ex);
        }

        return false;
    }

    public long addRecipe(String hash, Path jarfilePath, String title, String originalfile){

        long id = 0;

        try(PreparedStatement pst = getConnection().prepareStatement(
                "insert into recipes (hash, title, packedfile, filesize, dateadded, originalfilename) " +
                "values (?, ?, ?, ?, ?, ?)");
            InputStream is = Files.newInputStream(jarfilePath, StandardOpenOption.READ)
                ){

            pst.setString(1, hash);
            pst.setString(2, title);
            pst.setBinaryStream(3, is);
            pst.setLong(4, jarfilePath.toFile().length());
            pst.setLong(5, new Date().getTime());
            pst.setString(6, originalfile);

            pst.executeUpdate();

            try(ResultSet rs = pst.getGeneratedKeys()){
                if(rs.next()){
                    id = rs.getLong(1);
                }
            }
        } catch (IOException | SQLException ex) {
            log.error("failed to add recipe", ex);
        }

        return id;
    }

    public List<Recipe> getAllRecipes(){
        final ArrayList<Recipe> recipes = new ArrayList<Recipe>();

        try(Statement st = getConnection().createStatement();
                ResultSet rs = st.executeQuery("select id, hash, title from recipes")
                ){

            while(rs.next()){
                final Recipe r = new Recipe();
                r.setId(rs.getLong("id"));
                r.setHash(rs.getString("hash"));
                r.setTitle(rs.getString("title"));
                recipes.add(r);
            }

        } catch (SQLException e) {
            log.error("", e);
        }

        return recipes;
    }

    public void extractRecipeFile(String hash, File targetEmptyFile){

        try(PreparedStatement pst = getConnection().prepareStatement("select packedfile from recipes where hash = ?")){
            pst.setString(1, hash);

            try(ResultSet rs = pst.executeQuery()){
                while(rs.next()){
                    // one row and file is expected
                    try(InputStream is = rs.getBinaryStream("packedfile")){
                        is.transferTo(Files.newOutputStream(targetEmptyFile.toPath(), StandardOpenOption.CREATE));
                    }
                }
            }
        } catch (SQLException | IOException ex) {
            log.error("failed", ex);
        }
    }

    public List<Tag> getRootTags(){
        final ArrayList<Tag> tags = new ArrayList<Tag>();

        try(Statement st = getConnection().createStatement();
                ResultSet rs = st.executeQuery(
                        "select id, parentid, val, specialid" +
                        " from tags where parentid is null" +
                        " order by displayorder, val")
                ){

            while(rs.next()){
                final Tag t = new Tag();
                t.setId(rs.getLong("id"));
                t.setParentid(rs.getLong("parentid"));
                t.setVal(rs.getString("val"));
//                int i = rs.getInt("specialid");	// 0 if null
//                t.setSpecialid(i);
                t.setSpecialid(rs.getInt("specialid"));
                tags.add(t);
            }

        } catch (SQLException e) {
            log.error("", e);
        }

        return tags;
    }

    public List<Tag> getChildrenTags(String tag){
        final ArrayList<Tag> tags = new ArrayList<Tag>();

        final String sql =
"select child.id, child.parentid, child.val" +
" from tags t" +
" left join tags child on child.parentid = t.id" +
" where child.parentid is not null and t.val = ?";

        try(PreparedStatement pst = getConnection().prepareStatement(sql)){
            pst.setString(1, tag);
            try(ResultSet rs = pst.executeQuery()){
                while(rs.next()){
                    final Tag t = new Tag();
                    t.setId(rs.getLong("id"));
                    t.setParentid(rs.getLong("parentid"));
                    t.setVal(rs.getString("val"));
                    tags.add(t);
                }
            }
        } catch (SQLException e) {
            log.error("", e);
        }

        return tags;
    }

    public List<Recipe> getRecipesWithoutTags(){
        final ArrayList<Recipe> recipes = new ArrayList<>();

        try(Statement st = getConnection().createStatement();
                ResultSet rs = st.executeQuery(
                        "select hash, title from recipes r" +
                        " left join recipetags rt on rt.recipeid = r.id" +
                        " where rt.tagid is null order by dateadded")
                ){

            while(rs.next()){
                final Recipe r = new Recipe();
                r.setHash(rs.getString("hash"));
                r.setTitle(rs.getString("title"));
                recipes.add(r);
            }

        } catch (SQLException e) {
            log.error("", e);
        }

        return recipes;
    }

    public List<Recipe> getRecipesByTag(String tag){
        final ArrayList<Recipe> recipes = new ArrayList<>();

        try(PreparedStatement pst = getConnection().prepareStatement(
                "select hash, title from recipes r" +
                " left join recipetags rt on rt.recipeid = r.id" +
                " left join tags t on t.id = rt.tagid" +
                " where t.val = ? order by dateadded")
                ){

            pst.setString(1, tag);
            try(ResultSet rs = pst.executeQuery()){
                while(rs.next()){
                    final Recipe r = new Recipe();
                    r.setHash(rs.getString("hash"));
                    r.setTitle(rs.getString("title"));
                    recipes.add(r);
                }
            }
        } catch (SQLException e) {
            log.error("", e);
        }

        return recipes;
    }

    /**
     * From given list, create tags that do not exist
     *
     * @throws SQLException
     */
    private void addMissingTags(List<String> tags) throws SQLException{

        try(PreparedStatement pstCheck = getConnection().prepareStatement("select id from tags where val = ?");
                PreparedStatement pstAdd = getConnection().prepareStatement("insert into tags (val, displayorder) values (?, ?)")
                ){

            getConnection().setAutoCommit(false); // TODO is autocommit off needed for batch?

            for(String tag: tags){
                pstCheck.setString(1, tag);
                try(ResultSet rs = pstCheck.executeQuery()){
                    if(!rs.next()){
                        pstAdd.setString(1, tag);
                        pstAdd.setInt(2, 1);
                        pstAdd.addBatch();
                    }
                }
            }

            pstAdd.executeBatch();
            getConnection().commit();

        } catch (SQLException e) {
            log.error("", e);
        }
        finally {
            getConnection().setAutoCommit(true);
        }
    }

    public void updateRecipeTags(final String hash, final List<String> tags){

        try(PreparedStatement pst = getConnection().prepareStatement("select id from recipes where hash = ?")){

            addMissingTags(tags);

            pst.setString(1, hash);
            try(ResultSet rs = pst.executeQuery()){
                if(rs.next()){
                    final long id = rs.getLong("id");

                    try(PreparedStatement pstDel = getConnection().prepareStatement("delete from recipetags where recipeid = ?");
                            PreparedStatement pstIns = getConnection().prepareStatement("insert into recipetags (recipeid, tagid) values (?, (select id from tags where val = ?))")
                            ){

                        pstDel.setLong(1, id);
                        pstDel.executeUpdate();

                        pstIns.setLong(1, id);
                        for(String tag: tags){
                            pstIns.setString(2, tag);
                            pstIns.addBatch();
                        }
                        pstIns.executeBatch();
                    }
                }
            }

        } catch (SQLException e) {
            log.error("", e);
        }
    }

    public List<String> getRecipeTags(final String hash){
        final ArrayList<String> tags = new ArrayList<>();


        try(PreparedStatement pst = getConnection().prepareStatement(
                "select val from tags t" +
                " left join recipetags rt on rt.tagid = t.id" +
                " left join recipes r on r.id = rt.recipeid" +
                " where r.hash = ?" +
                " order by val")){

            pst.setString(1, hash);
            try(ResultSet rs = pst.executeQuery()){
                while(rs.next()){
                    tags.add(rs.getString("val"));
                }
            }

        } catch (SQLException e) {
            log.error("", e);
        }

        return tags;
    }

    public void updateRecipe(final String hash, final String newTitle){
        try(PreparedStatement pst = getConnection().prepareStatement(
                "update recipes set title = ? where hash = ?")) {

            pst.setString(1, newTitle);
            pst.setString(2, hash);
            pst.executeUpdate();

        } catch (SQLException e) {
            log.error("", e);
        }
    }
}
