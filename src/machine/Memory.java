/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package machine;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jsanchez
 */
public final class Memory {
    private final int PAGE_SIZE = 0x4000;
    
    private int[] Rom48k = new int[PAGE_SIZE];
    private int[][] Rom128k = new int[2][PAGE_SIZE];
    private int[][] RomPlus2 = new int[2][PAGE_SIZE];
    private int[][] RomPlus3 = new int[4][PAGE_SIZE];
    // 8 p�ginas de RAM
    private int[][] Ram = new int[8][PAGE_SIZE];
    // RAM falsa para dejar que escriba en p�ginas de ROM sin afectar a la
    // ROM de verdad. Esto evita tener que comprobar en cada escritura si la
    // p�gina es de ROM o de RAM.
    private int[] fakeROM = new int[PAGE_SIZE];
    // punteros a las 4 p�ginas
    private int[][] readPages = new int[4][];
    private int[][] writePages = new int[4][];
    // N�mero de p�gina de RAM de donde sale la pantalla activa
    private int screenPage, highPage, bankM, bankP;
    private boolean model128k, pagingLocked, plus3RamMode;
    MachineTypes spectrumModel;

    public Memory() {
        spectrumModel = MachineTypes.SPECTRUM48K;
        reset();
    }

    public void setSpectrumModel(MachineTypes model) {
        spectrumModel = model;
    }

    public int readScreenByte(int address) {
        return Ram[screenPage][address & 0x3fff];
    }

    public int readByte(int address) {
        return readPages[address >>> 14][address & 0x3fff];
    }

    public void writeByte(int address, int value) {
        writePages[address >>> 14][address & 0x3fff] = value;
    }

    public int readByte(int page, int address) {
        return Ram[page][address & 0x3fff];
    }

    public void writeByte(int page, int address, int value) {
        Ram[page][address & 0x3fff] = value;
    }

    private void setMemoryMap48k() {
        readPages[0] = Rom48k;
        readPages[1] = Ram[5];
        readPages[2] = Ram[2];
        readPages[3] = Ram[0];

        writePages[0] = fakeROM;
        writePages[1] = Ram[5];
        writePages[2] = Ram[2];
        writePages[3] = Ram[0];

        screenPage = 5;
        model128k = false;
    }

    private void setMemoryMap128k() {
        readPages[0] = Rom128k[0];
        writePages[0] = fakeROM;

        readPages[1] = writePages[1] = Ram[5];
        readPages[2] = writePages[2] = Ram[2];
        readPages[3] = writePages[3] = Ram[0];

        screenPage = 5;
        highPage = 0;
        model128k = true;
        pagingLocked = plus3RamMode = false;
        bankM = 0;
    }

    private void setMemoryMapPlus2() {
        readPages[0] = RomPlus2[0];
        writePages[0] = fakeROM;

        readPages[1] = writePages[1] = Ram[5];
        readPages[2] = writePages[2] = Ram[2];
        readPages[3] = writePages[3] = Ram[0];

        screenPage = 5;
        highPage = 0;
        model128k = true;
        pagingLocked = plus3RamMode = false;
        bankM = 0;
    }

    private void setMemoryMapPlus3() {
        readPages[0] = RomPlus3[0];
        writePages[0] = fakeROM;

        readPages[1] = writePages[1] = Ram[5];
        readPages[2] = writePages[2] = Ram[2];
        readPages[3] = writePages[3] = Ram[0];

        screenPage = 5;
        highPage = 0;
        model128k = true;
        pagingLocked = plus3RamMode = false;
        bankM = bankP = 0;
    }

    public void setPort7ffd(int port7ffd) {
//        System.out.println(String.format("port7ffd = %02x", port7ffd));
        if (pagingLocked || port7ffd == bankM)
            return;

        bankM = port7ffd;

        // Set the page locking state
        pagingLocked = (port7ffd & 0x20) == 0 ? false : true;

        // Set the active screen
        screenPage = (port7ffd & 0x08) == 0 ? 5 : 7;

        if (plus3RamMode)
            return;

        // Set the high page
        highPage = port7ffd & 0x07;
        readPages[3] = writePages[3] = Ram[highPage];

        // Set the active ROM
        switch (spectrumModel) {
            case SPECTRUM128K:
                readPages[0] = (port7ffd & 0x10) == 0 ? Rom128k[0] : Rom128k[1];
                break;
            case SPECTRUMPLUS2:
                readPages[0] = (port7ffd & 0x10) == 0 ? RomPlus2[0] : RomPlus2[1];
                break;
            case SPECTRUMPLUS3:
                doPagingPlus3();
                break;
        }
    }

    public void setPort1ffd(int port1ffd) {
        port1ffd &= 0x07;
        if (pagingLocked || port1ffd == bankP)
            return;

        bankP = port1ffd;

        doPagingPlus3();
    }

    private void doPagingPlus3() {
        // Paging mode normal (bit0 of 0x1ffd to 0)
        if ((bankP & 0x01) == 0) {
            int rom = ((bankM & 0x10) >>> 4) | ((bankP & 0x04) >>> 1);

            readPages[0] = RomPlus3[rom];
            writePages[0] = fakeROM;
            plus3RamMode = false;
        } else {
            // Special paging mode (all pages are RAM)
            plus3RamMode = true;
            switch ((bankP & 0x06) >>> 1) {
                case 0:
                    readPages[0] = Ram[0];
                    writePages[0] = Ram[0];
                    readPages[1] = Ram[1];
                    writePages[1] = Ram[1];
                    readPages[2] = Ram[2];
                    writePages[2] = Ram[2];
                    readPages[3] = Ram[3];
                    writePages[3] = Ram[3];
                    highPage = 3;
                    break;
                case 1:
                    readPages[0] = Ram[4];
                    writePages[0] = Ram[4];
                    readPages[1] = Ram[5];
                    writePages[1] = Ram[5];
                    readPages[2] = Ram[6];
                    writePages[2] = Ram[6];
                    readPages[3] = Ram[7];
                    writePages[3] = Ram[7];
                    highPage = 7;
                    break;
                case 2:
                    readPages[0] = Ram[4];
                    writePages[0] = Ram[4];
                    readPages[1] = Ram[5];
                    writePages[1] = Ram[5];
                    readPages[2] = Ram[6];
                    writePages[2] = Ram[6];
                    readPages[3] = Ram[3];
                    writePages[3] = Ram[3];
                    highPage = 3;
                    break;
                case 3:
                    readPages[0] = Ram[4];
                    writePages[0] = Ram[4];
                    readPages[1] = Ram[7];
                    writePages[1] = Ram[7];
                    readPages[2] = Ram[6];
                    writePages[2] = Ram[6];
                    readPages[3] = Ram[3];
                    writePages[3] = Ram[3];
                    highPage = 3;
                    break;
            }
        }
    }

    public int getPlus3HighPage() {
        return highPage;
    }

//    public int getPlus3ScreenPage() {
//        return screenPage;
//    }

    public boolean getPlus3RamMode() {
        return plus3RamMode;
    }

    // "La Abad�a del Crimen" pone la p�gina 7 en 0xC000 y selecciona la
    // pantalla de la p�gina 7. A partir de ah�, la modifica en 0xC000 en
    // lugar de hacerlo en 0x4000, donde siempre est� la p�gina 5.
    public boolean isScreenByte(int addr) {
        switch(addr >>> 14) {
            case 0: // Address 0x0000-0x3fff
            case 2: // Address 0x8000-0xbfff
                return false;
            case 1: // Address 0x4000-0x7fff
                if (addr > 0x5aff)
                    return false;
                break;
            case 3: // Address 0xc000-0xffff
                if (!model128k || highPage != screenPage || addr > 0xdaff)
                    return false;
        }
        return true;
    }

    public void setScreenPage(int nPage) {
        screenPage = nPage;
    }

    public void reset() {
        switch(spectrumModel) {
            case SPECTRUM48K:
                setMemoryMap48k();
                break;
            case SPECTRUM128K:
                setMemoryMap128k();
                break;
            case SPECTRUMPLUS2:
                setMemoryMapPlus2();
                break;
            case SPECTRUMPLUS3:
                setMemoryMapPlus3();
        }
    }

    public void loadRoms() {
        if (!loadRomAsFile("spectrum.rom", Rom48k))
            loadRomAsResource("/roms/spectrum.rom", Rom48k);

        if (!loadRomAsFile("128-0.rom", Rom128k[0]))
            loadRomAsResource("/roms/128-0.rom", Rom128k[0]);
        if (!loadRomAsFile("128-1.rom", Rom128k[1]))
            loadRomAsResource("/roms/128-1.rom", Rom128k[1]);

        if (!loadRomAsFile("plus2-0.rom", RomPlus2[0]))
            loadRomAsResource("/roms/plus2-0.rom", RomPlus2[0]);
        if (!loadRomAsFile("plus2-1.rom", RomPlus2[1]))
            loadRomAsResource("/roms/plus2-1.rom", RomPlus2[1]);

        if (!loadRomAsFile("plus3-0.rom", RomPlus3[0]))
            loadRomAsResource("/roms/plus3-0.rom", RomPlus3[0]);
        if (!loadRomAsFile("plus3-1.rom", RomPlus3[1]))
            loadRomAsResource("/roms/plus3-1.rom", RomPlus3[1]);
        if (!loadRomAsFile("plus3-2.rom", RomPlus3[2]))
            loadRomAsResource("/roms/plus3-2.rom", RomPlus3[2]);
        if (!loadRomAsFile("plus3-3.rom", RomPlus3[3]))
            loadRomAsResource("/roms/plus3-3.rom", RomPlus3[3]);

    }

    private boolean loadRomAsResource(String filename, int[] page) {
        InputStream inRom;

        try {
            inRom = Spectrum.class.getResourceAsStream(filename);
            if (inRom == null) {
                String msg =
                    java.util.ResourceBundle.getBundle("machine/Bundle").getString(
                    "RESOURCE_ROM_ERROR");
                System.out.println(String.format("%s: %s", msg, filename));
                return false;
            }

            int count, value;
            for (count = 0; count < 0x4000; count++) {
                value = inRom.read();
                if (value == -1)
                    break;
                page[count] = value & 0xff;
            }

            if (count != 0x4000) {
                String msg =
                    java.util.ResourceBundle.getBundle("machine/Bundle").getString(
                    "ROM_SIZE_EROR");
                System.out.println(String.format("%s: %s", msg, filename));
                return false;
            }

            inRom.close();
        } catch (IOException ex) {
            String msg =
                java.util.ResourceBundle.getBundle("machine/Bundle").getString(
                "RESOURCE_ROM_ERROR");
            System.out.println(String.format("%s: %s", msg, filename));
            Logger.getLogger(Spectrum.class.getName()).log(Level.SEVERE, null, ex);
        }
        String msg =
            java.util.ResourceBundle.getBundle("machine/Bundle").getString(
            "ROM_RESOURCE_LOADED");
        System.out.println(String.format("%s: %s", msg, filename));

        return true;
    }

    private boolean loadRomAsFile(String filename, int[] page) {
        BufferedInputStream fIn;

        try {
            try {
                fIn = new BufferedInputStream(new FileInputStream(filename));
            } catch (FileNotFoundException ex) {
                String msg =
                java.util.ResourceBundle.getBundle("machine/Bundle").getString(
                "FILE_ROM_ERROR");
                System.out.println(String.format("%s: %s", msg, filename));
                //Logger.getLogger(Spectrum.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }

            int count, value;
            for (count = 0; count < 0x4000; count++) {
                value = fIn.read();
                if (value == -1)
                    break;
                page[count] = value  & 0xff;
            }

            if (count != 0x4000) {
                String msg =
                    java.util.ResourceBundle.getBundle("machine/Bundle").getString(
                    "ROM_SIZE_EROR");
                System.out.println(String.format("%s: %s", msg, filename));
                return false;
            }

            fIn.close();
        } catch (IOException ex) {
            String msg =
                java.util.ResourceBundle.getBundle("machine/Bundle").getString(
                "FILE_ROM_ERROR");
            System.out.println(String.format("%s: %s", msg, filename));
            Logger.getLogger(Spectrum.class.getName()).log(Level.SEVERE, null, ex);
        }

        String msg =
            java.util.ResourceBundle.getBundle("machine/Bundle").getString(
            "ROM_FILE_LOADED");
        System.out.println(String.format("%s: %s", msg, filename));

        return true;
    }
}
