package jboy.system;

/**
 * Class for timer information
 *
 * For the TAC (Timer Control - 0xFF07) register, bit 2 determines if it is active or not and
 * bits 0 and 1 combine to determine the frequency:
 * 00:  4194304/4096 = 1024 clock cycles
 * 01:  4194304/262144 = 16 clock cycles
 * 10:  4194304/65536 =  64 clock cycles
 * 11:  4194304/16384 = 256 clock cycles
 */
class Timers {
    // The DIV counter is part of a 16 bit system counter, with DIV being the upper 8 bits. The default for the
    // system clock is 0xABCC.
    static int systemCounter = 0xABCC;
    static int timaCounter = 0x00;

    // The frequencies in Hz.
    public interface TAC {
        int CLOCK0 = 4096;
        int CLOCK1 = 262144;
        int CLOCK2 = 65536;
        int CLOCK3 = 16384;
    }

    private Memory memory;

    Timers(Memory memory) {
        this.memory = memory;
    }

    static int getFrequency(int frequency) {
        if(frequency == 0b00) {
            return TAC.CLOCK0;
        } else if(frequency == 0b01) {
            return TAC.CLOCK1;
        } else if(frequency == 0b10) {
            return TAC.CLOCK2;
        } else {
            return TAC.CLOCK3;
        }
    }

    void tick(int cycles) {
        this.incrementSystemCounter(cycles);

        int tac = this.memory.getByteAt(IORegisters.TAC);
        boolean isEnabled = (tac & 0x04) == 0x04;

        if(isEnabled) {
            int tacFreq = Timers.getFrequency(tac & 0x03);

            if(Timers.timaCounter >= (CPU.FREQUENCY / tacFreq)) {
                int tima = this.memory.getByteAt(IORegisters.TIMA) + 1;

                if(tima > 0xFF) {
                    // TODO: The interrupt is delayed by 4 clocks
                    // TODO: The value of TIMA is 0x00 during this delay, NOT the value in TMA
                    tima = this.memory.getByteAt(IORegisters.TMA);
                    int flags = this.memory.getByteAt(IORegisters.INTERRUPT_FLAGS);
                    flags |= Interrupts.TIMER;
                    this.memory.setByteAt(IORegisters.INTERRUPT_FLAGS, flags);
                }

                Timers.timaCounter = tima;
                this.memory.setByteAt(IORegisters.TIMA, tima);
            }
        }
    }

    private void incrementSystemCounter(int cycles) {
        Timers.systemCounter += cycles;

        if(Timers.systemCounter > 0xFFFF) {
            Timers.systemCounter = 0;
        }

        this.memory.updateDiv(Timers.systemCounter >> 8);
    }
}
