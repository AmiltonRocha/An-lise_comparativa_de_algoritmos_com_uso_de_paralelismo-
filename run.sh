#!/bin/bash
echo "Compilando..."
mkdir -p bin
javac -cp jocl-2.0.4.jar -d bin src/*.java
if [ $? -eq 0 ]; then
    echo "Compilado com sucesso!"
    echo ""
    PALAVRA=${1:-the}
    java -cp bin:jocl-2.0.4.jar AnalisadorTexto "$PALAVRA"
fi
