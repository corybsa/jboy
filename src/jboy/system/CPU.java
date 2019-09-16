package jboy.system;

import jboy.instructions.Instructions;
import jboy.instructions.Instruction;

import java.util.HashMap;

/**
 * <h3>Description</h3>
 * <h5>The GameBoy uses a chip that's a cross between the Intel 8080 and the Zilog Z80. The chip is the Sharp LR35902.</h5>
 *
 * <hr/>
 * <h3>Information about the CPU</h3>
 * <ul>
 *     <li>Number of instructions: 500</li>
 *     <li>
 *         Registers
 *         <ul>
 *             <li>8-bit: A, B, C, D, E, F, H, L</li>
 *             <li>16-bit: AF, BC, DE, HL, SP, PC</li>
 *         </ul>
 *     </li>
 *     <li>Clock speed: 4.194304 MHz (4.19 MHz)</li>
 * </ul>
 *
 * <hr/>
 * <h3>A few notes on the CPU:</h3>
 * <ul>
 *     <li>Official Nintendo documents refer to "machine cycles" when describing instructions.</li>
 *     <li>One machine cycle equals four CPU clock cycles.</li>
 *     <li>The numerical value of a machine cycle is 1.048576 MHz (1.05 MHz)</li>
 * </ul>
 *
 * <hr/>
 * <h3>A few notes on the registers:</h3>
 * <ul>
 *     <li>The F register is indirectly accessible by the programmer, and is used to store the results of various math operations.</li>
 *     <li>The PC register points to the next instruction to be executed in memory.</li>
 *     <li>The SP register points to the current stack position.</li>
 *     <li>
 *         The F register consists of the following:
 *         <ul>
 *             <li>Zero flag (Z): This bit is set when the result of a math operation is zero or two values match when using the CP instruction.</li>
 *             <li>Subtract flag (N): This bit is set if a subtraction was performed in the last math instruction.</li>
 *             <li>Half carry flag (H): This bit is set if a carry occurred from the lower nibble in the last math operation.</li>
 *             <li>Carry flag (C): This bit is set if a carry occurred from the last math operation of if register A is the smaller value when executing the CP instruction.</li>
 *         </ul>
 *     </li>
 *     <li>
 *         On power up, the PC is initialized to 0x100 and the instruction at that location in the ROM is executed.
 *         From here on the PC is controlled indirectly by the instructions themselves that were generated by the programmer of the ROM cart.
 *     </li>
 *     <li>
 *         The SP is used to keep track of the top of the stack.
 *         <ul>
 *             <li>The Stack is used for saving variables, saving return addressed, passing arguments to subroutines and various other uses.</li>
 *             <li>The instructions CALL, PUSH and RST all put information onto the stack.</li>
 *             <li>The instructions POP, RET and RETI all take information off of the stack.</li>
 *             <li>Interrupts put a return address on the stack and remove it at the completion as well.</li>
 *             <li>
 *                 As information is put onto the stack, the stack grows DOWNWARD in RAM. As a result SP should always be initialized at the highest location of RAM space that has been allocated for use byu the stack.
 *                 <ul>
 *                     <li>
 *                         For example, if a programmer wants to locate the SP at the top of low RAM space (0xC000 - 0xDFFF) he would set SP to 0xE000 using LD SP,$E000.
 *                         (The SP automatically decrements before it puts something onto the stack, so it is perfectly acceptable to assign it a value which points to a memory address which is one location past the end of available RAM.)
 *                     </li>
 *                     <li>The SP is initialized to 0xFFFE on power up, but a programmer should not rely on this setting and should explicitly set its value.</li>
 *                 </ul>
 *             </li>
 *         </ul>
 *     </li>
 * </ul>
 */
public class CPU {
    private byte A;
    private byte B;
    private byte C;
    private byte D;
    private byte E;
    private byte F;
    private byte H;
    private byte L;
    private short AF;
    private short BC;
    private short DE;
    private short HL;
    private short SP;
    private short PC;
    private Memory memory;
    private HashMap<?, Instruction> instructions;

    interface Command {
        void exec(Object data);
    }

    public CPU() {
        this.memory = new Memory();
        this.PC = 0x100;
    }

    private void incrementPC(byte n) {
        this.PC += n;
    }

    public void loadROM(byte[] rom) {
        this.memory.loadROM(rom);
        this.tick();
    }

    public void tick() {
        Instruction instruction = Instructions.GB_8BIT_INSTRUCTIONS.get(this.memory.getByteAt(this.PC));
        this.execute(instruction);
    }

    private void execute(Instruction instruction) {
        switch(instruction.getOpType()) {
            case MISC:
                this.executeMisc(instruction);
                break;
            case LOAD_8BIT:
                break;
            case LOAD_16BIT:
                break;
            case MATH_8BIT:
                break;
            case MATH_16BIT:
                break;
            case JUMP:
                break;
            default:
                break;
        }

        this.incrementPC(instruction.getOpSize());
    }

    private void executeMisc(Instruction instruction) {
        switch(instruction.getOpCode()) {
            case 0x00:
                this.nop();
                break;
        }
    }

    /**
     * No operation.
     */
    private void nop() {
        // nothing.
    }

    /**
     * Load {@code n} into B.
     * @param n An 8-bit immediate value.
     */
    public void ld_b_n(byte n) {
        this.B = n;
    }

    /**
     * Load {@code n} into C.
     * @param n An 8-bit immediate value.
     */
    public void ld_c_n(byte n) {
        this.C = n;
    }

    /**
     * Load {@code n} into D.
     * @param n An 8-bit immediate value.
     */
    public void ld_d_n(byte n) {
        this.D = n;
    }

    /**
     * Load {@code n} into E.
     * @param n An 8-bit immediate value.
     */
    public void ld_e_n(byte n) {
        this.E = n;
    }

    /**
     * Load {@code n} into H.
     * @param n An 8-bit immediate value.
     */
    public void ld_h_n(byte n) {
        this.H = n;
    }

    /**
     * Load {@code n} into L.
     * @param n An 8-bit immediate value.
     */
    public void ld_l_n(byte n) {
        this.L = n;
    }
}
