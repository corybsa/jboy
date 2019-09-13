package jboy.disassembler;

import java.util.ArrayList;

import static jboy.disassembler.Instructions.GB_16BIT_INSTRUCTIONS;
import static jboy.disassembler.Instructions.GB_8BIT_INSTRUCTIONS;

/**
 * Class for disassembling GameBoy roms.
 */
public class Disassembler {
    private byte[] bytes;
    private int pc;
    private ArrayList<String> instructions;

    public Disassembler(byte[] bytes) {
        this.bytes = bytes;
        this.instructions = new ArrayList<>();
    }

    /**
     * Disassembles the rom and returns a string representation of the disassembly.
     */
    public void disassemble() {
        try {
            Instruction<Byte> ins8Bit;
            Instruction<Short> ins16Bit;
            this.pc = 0x100;

            while(this.pc < this.bytes.length) {
                if(this.pc < 0) {
                    break;
                }

                // 16 bit instructions are at 0xCB, so we need to do something slightly different with those.
                if((this.bytes[this.pc] & 0xFF) != 0xCB) {
                    ins8Bit = GB_8BIT_INSTRUCTIONS.get(this.bytes[this.pc]);
                    this.instructions.add(
                            ins8Bit.getOpHex()
                            .concat("\t\t")
                            .concat(this.parseInstruction(ins8Bit).getInstruction())
                    );

                    this.pc += ins8Bit.getOpSize();
                } else {
                    // To get the proper op code we need to take the current byte at the program counter and shift it left 8 bits.
                    // Next we need to get the following byte, so pc + 1 and use bitwise-and 0xFF to pretend that it's unsigned..
                    // Once we do that we just combine the two bytes with a bitwise-or.
                    short op = (short)((bytes[this.pc] << 8) | (bytes[this.pc + 1] & 0xFF));
                    ins16Bit = GB_16BIT_INSTRUCTIONS.get(op);
                    this.instructions.add(
                            ins16Bit.getOpHex()
                            .concat("\t")
                            .concat(ins16Bit.getOpName())
                    );

                    this.pc += ins16Bit.getOpSize();
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Parse instruction definition, filling in any parameters.
     * @param instruction The {@link Instruction} to parse.
     * @return The parsed {@link Instruction}.
     */
    private Instruction<Byte> parseInstruction(Instruction<Byte> instruction) {
        byte op1;
        byte op2;

        switch(instruction.getOpSize()) {
            case 2:
                // ignore 0xE2, 0xF2 and 0x10. These are all 2 bytes, but they don't have any arguments.
                if(
                        (instruction.getOpCode() & 0xFF) == 0xE2 ||
                        (instruction.getOpCode() & 0xFF) == 0xF2 ||
                        instruction.getOpCode() == 0x10
                ) {
                    op1 = (byte)0xD3; // See comment at the end of the method.
                    op2 = (byte)0xD3; // See comment at the end of the method.
                } else {
                    op1 = this.bytes[this.pc + 1];
                    op2 = (byte)0xD3; // See comment at the end of the method.
                }
                break;
            case 3:
                op1 = this.bytes[this.pc + 2];
                op2 = this.bytes[this.pc + 1];
                break;
            default:
                op1 = (byte)0xD3; // See comment at the end of the method.
                op2 = (byte)0xD3; // See comment at the end of the method.
                break;
        }

        // 0xD3 does not exist on the GB CPU. So this will let us know that we don't need to do anything with this op.

        return instruction.parseInstruction(op1, op2);
    }

    /**
     * Returns the instructions in an array list.
     * @return The instructions
     */
    public ArrayList<String> getDisassemblyList() {
        return this.instructions;
    }
}