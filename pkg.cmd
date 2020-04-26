

call gradlew.bat clean installdist

: delete empty jars to fix jlink issue when modular

: set FXVERSION=15-ea+4
set FXVERSION=14.0.1

pushd build\install\cookbook\lib
: del ^
 javafx-base-%FXVERSION%.jar ^
 javafx-controls-%FXVERSION%.jar ^
 javafx-graphics-%FXVERSION%.jar ^
 javafx-media-%FXVERSION%.jar


: https://stackoverflow.com/questions/889518/set-the-value-of-a-variable-with-the-result-of-a-command-in-a-windows-batch-file
dir /b cookbook*.jar > ..\mainjar.txt
set /p MAINJAR=< ..\mainjar.txt

popd

echo ===%MAINJAR%===

set PATH=%PATH%;c:\bin\wix311-binaries

: nonmodular

: --main-class org.sergeys.cookbook.ui.CookBookLauncher

jpackage ^
--input build/install/cookbook/lib ^
--main-jar %MAINJAR% ^
 ^
--verbose ^
--dest build/install ^
--name "CookBook" ^
--app-version "0.0.4" ^
--icon src/main/resources/images/amor.ico ^
--vendor "Sergey Selivanov" ^
--win-shortcut ^
--win-dir-chooser ^
--win-menu ^
--win-menu-group "CookBook"


goto end

: modular, fails because of h2

jpackage ^
--module-path build/install/cookbook/lib ^
--module cookbook/org.sergeys.cookbook.ui.CookBook ^
 ^
--verbose ^
--dest build/install ^
--name "Cook Book" ^
--app-version "0.0.3" ^
--icon src/main/resources/images/amor.ico ^
--vendor "Svs" ^
--win-shortcut ^
--win-dir-chooser ^
--win-menu ^
--win-menu-group "Svs"

:end
