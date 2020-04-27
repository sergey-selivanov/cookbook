<map version="1.0.1">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node CREATED="1584842156287" ID="ID_883179019" MODIFIED="1584842164774" TEXT="Cookbook">
<node CREATED="1584842195536" ID="ID_1126533596" MODIFIED="1584842198865" POSITION="right" TEXT="TODO">
<node CREATED="1584897738058" ID="ID_743078963" MODIFIED="1584897750075" TEXT="html parsing">
<node CREATED="1584842199814" ID="ID_1884996938" MODIFIED="1587888954110" TEXT="try jsoup to parse and modify html? instead of xerces">
<icon BUILTIN="button_ok"/>
<node CREATED="1584842301766" ID="ID_1567968615" MODIFIED="1584901026642" TEXT="Sanitize untrusted HTML">
<icon BUILTIN="button_cancel"/>
</node>
</node>
<node CREATED="1584896374233" ID="ID_1407806878" MODIFIED="1587889006941" TEXT="remove or replace http:// links and images which can&apos;t be downloaded or failed, to placeholder"/>
<node CREATED="1584897699030" ID="ID_1459105573" MODIFIED="1584897728224" TEXT="skip included files referenced in deleted e.g script tags"/>
</node>
<node CREATED="1584921979506" ID="ID_1403350064" MODIFIED="1584921981584" TEXT="UI">
<node CREATED="1584896627766" ID="ID_821771700" MODIFIED="1587889017356" TEXT="put vertical divider to non zero position on 1st start">
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1587789554078" ID="ID_701394392" MODIFIED="1587889020278" TEXT="app icon and titile in the window">
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1584921987568" ID="ID_1417423787" MODIFIED="1584922013760" TEXT="indicate wait and progress on import"/>
<node CREATED="1587789185264" ID="ID_1449283039" MODIFIED="1587789196583" TEXT="modern ui, burger menu etc">
<node CREATED="1587992571089" ID="ID_892413157" LINK="https://github.com/HanSolo/tilesfx" MODIFIED="1587992571089" TEXT="https://github.com/HanSolo/tilesfx">
<node CREATED="1587992604264" LINK="https://mvnrepository.com/artifact/eu.hansolo/tilesfx" MODIFIED="1587992604264" TEXT="https://mvnrepository.com/artifact/eu.hansolo/tilesfx"/>
</node>
</node>
<node CREATED="1587789150529" ID="ID_782698278" MODIFIED="1587789583178" TEXT="use modern icons on buttons">
<node CREATED="1587789157864" ID="ID_1517802025" MODIFIED="1587789462435" TEXT="find and use icons jar">
<node CREATED="1587992541684" ID="ID_1257272177" LINK="https://github.com/kordamp/ikonli" MODIFIED="1587992541684" TEXT="https://github.com/kordamp/ikonli">
<node CREATED="1587992628773" LINK="https://mvnrepository.com/artifact/org.kordamp.ikonli" MODIFIED="1587992628773" TEXT="https://mvnrepository.com/artifact/org.kordamp.ikonli"/>
</node>
</node>
</node>
</node>
<node CREATED="1587889072372" ID="ID_1793013010" MODIFIED="1587889101373" TEXT="include version and build info properties"/>
<node CREATED="1587992651707" ID="ID_872089590" MODIFIED="1587993132906" TEXT="context menu on tree items">
<node CREATED="1587992900425" ID="ID_651284208" MODIFIED="1587992914166" TEXT="mark/highlight selected"/>
<node CREATED="1587992667882" ID="ID_1259101360" MODIFIED="1587992992425" TEXT="select/deselect item"/>
<node CREATED="1587992687079" ID="ID_1165452171" MODIFIED="1587992705430" TEXT="select/deselect all in tag"/>
<node CREATED="1587992779101" ID="ID_885429715" MODIFIED="1587992786139" TEXT="delete selection"/>
<node CREATED="1587992787360" ID="ID_124843938" MODIFIED="1587992791609" TEXT="export selection">
<node CREATED="1587992792480" ID="ID_1420041225" MODIFIED="1587993105846" TEXT="to db file">
<linktarget COLOR="#b0b0b0" DESTINATION="ID_1420041225" ENDARROW="Default" ENDINCLINATION="293;0;" ID="Arrow_ID_1426341861" SOURCE="ID_1676780731" STARTARROW="None" STARTINCLINATION="293;0;"/>
</node>
<node CREATED="1587992798946" ID="ID_937433394" MODIFIED="1587992805196" TEXT="as jars"/>
<node CREATED="1587992810928" ID="ID_1498290106" MODIFIED="1587992814827" TEXT="to dir"/>
</node>
</node>
<node CREATED="1587992941851" ID="ID_954482853" MODIFIED="1587992946309" TEXT="select items">
<node CREATED="1587992731107" ID="ID_745393880" MODIFIED="1587992955831" TEXT="with checkboxes">
<node CREATED="1587992746466" ID="ID_1311774319" MODIFIED="1587992768087" TEXT="show checkboxes with Ctrl"/>
</node>
<node CREATED="1587992890660" ID="ID_1931263064" MODIFIED="1587992896266" TEXT="with ctrl+click"/>
<node CREATED="1587992974310" ID="ID_366670796" MODIFIED="1587993132906" TEXT="via context menu"/>
</node>
<node CREATED="1587992218526" ID="ID_1095631101" MODIFIED="1587992248675" TEXT="open other db file, compare, import missing recipes"/>
<node CREATED="1587789209593" ID="ID_1077488781" MODIFIED="1587789437251" TEXT="copy recipes to other db file">
<linktarget COLOR="#b0b0b0" DESTINATION="ID_1077488781" ENDARROW="Default" ENDINCLINATION="135;0;" ID="Arrow_ID_1689615997" SOURCE="ID_1676780731" STARTARROW="None" STARTINCLINATION="135;0;"/>
</node>
<node CREATED="1584842918172" FOLDED="true" ID="ID_1539987114" MODIFIED="1587992409765" TEXT="generate installers">
<icon BUILTIN="button_ok"/>
<node CREATED="1587914076985" ID="ID_1319999710" MODIFIED="1587919809913" TEXT="non-modular packaging because of h2">
<icon BUILTIN="button_ok"/>
<node CREATED="1587914151598" ID="ID_1265877550" LINK="https://badass-jlink-plugin.beryx.org/releases/latest/" MODIFIED="1587914169579" TEXT="jlink plugin"/>
<node CREATED="1587914179377" ID="ID_1016000389" LINK="https://badass-runtime-plugin.beryx.org/releases/latest/" MODIFIED="1587914188890" TEXT="runtime plugin"/>
</node>
<node CREATED="1587919788557" ID="ID_91779121" MODIFIED="1587992394129" TEXT="platform specifics">
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1587890060365" ID="ID_1345057078" MODIFIED="1587992394128" TEXT="jenkins build">
<icon BUILTIN="button_ok"/>
</node>
</node>
<node CREATED="1584843045443" ID="ID_562658722" MODIFIED="1587789324142" TEXT="use hibernate"/>
<node CREATED="1584843066257" ID="ID_45678986" MODIFIED="1587789321086" TEXT="use flywaydb"/>
<node CREATED="1587890600355" ID="ID_1173560764" MODIFIED="1587890644808" TEXT="use spotbugs and checkstyle, pmd"/>
<node CREATED="1584896707850" ID="ID_1676780731" MODIFIED="1587993105846" TEXT="migrate h2 db to newest format if old detected">
<arrowlink DESTINATION="ID_1077488781" ENDARROW="Default" ENDINCLINATION="135;0;" ID="Arrow_ID_1689615997" STARTARROW="None" STARTINCLINATION="135;0;"/>
<arrowlink DESTINATION="ID_1420041225" ENDARROW="Default" ENDINCLINATION="293;0;" ID="Arrow_ID_1426341861" STARTARROW="None" STARTINCLINATION="293;0;"/>
</node>
<node CREATED="1587912869757" ID="ID_1707158386" MODIFIED="1587912892099" TEXT="splash screen, preloader, background unpack">
<node CREATED="1587912914420" ID="ID_802295243" LINK="https://www.genuinecoder.com/javafx-splash-screen-loading-screen/" MODIFIED="1587912931867" TEXT=""/>
<node CREATED="1587912954761" ID="ID_387067235" LINK="https://stackoverflow.com/questions/14972199/how-to-create-splash-screen-with-transparent-background-in-javafx" MODIFIED="1587912970100" TEXT=""/>
</node>
<node CREATED="1587992085494" FOLDED="true" ID="ID_863144707" MODIFIED="1587993188022" TEXT="Old todo">
<node CREATED="1587992110635" MODIFIED="1587992110635" TEXT="- context menu for recipe, selection of recipes and branch"/>
<node CREATED="1587992110637" MODIFIED="1587992110637" TEXT="- export recipe, export branch to dir"/>
<node CREATED="1587992110637" MODIFIED="1587992110637" TEXT="- &quot;like&quot; or &quot;heart&quot; button to set favorite; button to unset favorite"/>
<node CREATED="1587992110640" MODIFIED="1587992110640" TEXT="- delete recipes"/>
<node CREATED="1587992110640" MODIFIED="1587992110640" TEXT="- show progress of mass import, support cancel"/>
<node CREATED="1587992110641" MODIFIED="1587992110641" TEXT="- show message when warnings or errors on mass import"/>
<node CREATED="1587992110642" MODIFIED="1587992110642" TEXT="- colorise log output"/>
<node CREATED="1587992110642" MODIFIED="1587992110642" TEXT="- editor"/>
<node CREATED="1587992110643" MODIFIED="1587992110643" TEXT="- copy-paste from browser as html, save external images locally"/>
<node CREATED="1587992110644" MODIFIED="1587992110644" TEXT="- share recipes via xmpp"/>
<node CREATED="1587992110644" MODIFIED="1587992110644" TEXT="- share recipes p2p if possible, lookup via my server"/>
<node CREATED="1587992110645" MODIFIED="1587992110645" TEXT="- share recipes via google drive"/>
<node CREATED="1587992110646" MODIFIED="1587992110646" TEXT="- mass import, by file mask"/>
<node CREATED="1587992110646" MODIFIED="1587992110646" TEXT="- save and import currently viewed page as new recipe"/>
<node CREATED="1587992110647" MODIFIED="1587992110647" TEXT="- mark recently added recipes in the tree, or collect say last 20 under special branch"/>
<node CREATED="1587992110647" MODIFIED="1587992110647" TEXT="- quick text filter in tree"/>
<node CREATED="1587992110648" MODIFIED="1587992110648" TEXT="- cancel background tasks on exit"/>
<node CREATED="1587992110649" MODIFIED="1587992110649" TEXT="- show total # of recipes"/>
<node CREATED="1587992110650" MODIFIED="1587992110650" TEXT="- add existing tag to a recipe using context menu, show only those not already set"/>
<node CREATED="1587992110651" MODIFIED="1587992110651" TEXT="- try to keep tree state (what is expanded and selected) on buildtree, for example when tags changed on a recipe"/>
<node CREATED="1587992124053" ID="ID_399758966" MODIFIED="1587992126941" TEXT="Old links">
<node CREATED="1587992139072" LINK="http://stackoverflow.com/questions/11242847/is-drag-and-drop-supported-by-treeitem" MODIFIED="1587992139072" TEXT="stackoverflow.com &gt; Questions &gt; 11242847 &gt; Is-drag-and-drop-supported-by-treeitem"/>
<node CREATED="1587992139078" LINK="http://www.drdobbs.com/jvm/a-javafx-custom-container/232200699" MODIFIED="1587992139078" TEXT="autoresize issues: http://www.drdobbs.com/jvm/a-javafx-custom-container/232200699"/>
<node CREATED="1587992139082" LINK="http://stackoverflow.com/questions/10152828/javafx-2-automatic-column-width" MODIFIED="1587992139082" TEXT="stackoverflow.com &gt; Questions &gt; 10152828 &gt; Javafx-2-automatic-column-width"/>
<node CREATED="1587992139085" LINK="http://saidandem.blogspot.de/2012/01/percent-width-for-tablecolumn-in-javafx.html" MODIFIED="1587992139085" TEXT="saidandem.blogspot.de &gt; 2012 &gt; 01 &gt; Percent-width-for-tablecolumn-in-javafx"/>
<node CREATED="1587992139087" LINK="http://www.zenjava.com/2012/11/24/from-zero-to-javafx-in-5-minutes/" MODIFIED="1587992139087" TEXT="zenjava.com &gt; 2012 &gt; 11 &gt; 24 &gt; From-zero-to-javafx-in-5-minutes"/>
<node CREATED="1587992139089" LINK="http://htmlcleaner.sourceforge.net/javause.php" MODIFIED="1587992139089" TEXT="htmlcleaner.sourceforge.net &gt; Javause"/>
</node>
</node>
</node>
<node CREATED="1584842230232" ID="ID_1666087264" MODIFIED="1584842233213" POSITION="left" TEXT="Links">
<node CREATED="1584842323660" ID="ID_1566718536" LINK="https://jsoup.org/cookbook/introduction/parsing-a-document" MODIFIED="1584842373104" TEXT="https://jsoup.org/cookbook/introduction/parsing-a-document"/>
<node CREATED="1584844571104" ID="ID_1065359523" LINK="https://www.favicon-generator.org/" MODIFIED="1584844584211" TEXT="https://www.favicon-generator.org/"/>
<node CREATED="1587992481945" ID="ID_1867695618" LINK="https://openjfx.io/" MODIFIED="1587992481945" TEXT="https://openjfx.io/"/>
</node>
</node>
</map>
