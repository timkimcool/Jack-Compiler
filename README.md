# Jack Compiler
Two-tiered compiler along with an Assembeler.

## Compiler
Translates high-level Jack language into VM language. Input a .jack class file and a VM output file will be created.

## VM Translator
Translates VM language into hack assembly language. Input a text file containing VM commands and an .asm output file will be create.

## Assembler
Translates hack assembly language into binary code. Input an assembly file and an output file will be created with each line consisting of sixteen 0 and 1 characters.