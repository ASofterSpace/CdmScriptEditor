cd src\com\asofterspace

rd /s /q toolbox

md toolbox
cd toolbox

md configuration
md io
md web

cd ..\..\..\..

copy "..\Toolbox-Java\src\com\asofterspace\toolbox\*.java" "src\com\asofterspace\toolbox"
copy "..\Toolbox-Java\src\com\asofterspace\toolbox\configuration\*.*" "src\com\asofterspace\toolbox\configuration"
copy "..\Toolbox-Java\src\com\asofterspace\toolbox\io\*.*" "src\com\asofterspace\toolbox\io"
copy "..\Toolbox-Java\src\com\asofterspace\toolbox\web\*.*" "src\com\asofterspace\toolbox\web"

rd /s /q bin

md bin

cd src

dir /s /B *.java > sourcefiles.list

javac -d ../bin @sourcefiles.list

pause
