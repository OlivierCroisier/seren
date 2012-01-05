package net.thecodersbreakfast.seren;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;

/**
 * @author olivier
 */
public class SerenIT {

    public static final int NB_POJOS = 1000000;
    private static final int NB_RUNS = 4;

    @Test
    public void serializationSpeed() throws IOException {
        Pojo[] pojos = new Pojo[NB_POJOS];
        for (int i = 0; i < pojos.length; i++) {
            pojos[i] = new Pojo();
        }

        cleanMemory();

        for (int run = 0; run < NB_RUNS; run++) {
            ObjectOutputStream oos = new ObjectOutputStream(new NullOutputStream());
            long timeBefore = System.currentTimeMillis();
            for (Pojo pojo : pojos) {
                oos.writeObject(pojo);
            }
            oos.close();
            long timeAfter = System.currentTimeMillis();
            System.out.printf("%02d/%02d : %4d ms %n", run, NB_RUNS, timeAfter - timeBefore);
        }
    }

    @Test
    public void serializationCorrectness() throws IOException, ClassNotFoundException {
        Pojo pojoBefore = new Pojo();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(pojoBefore);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Pojo pojoAfter = (Pojo) ois.readObject();
        assertEquals(pojoBefore, pojoAfter);
    }

    private void cleanMemory() {
        System.gc();
        System.gc();
        System.gc();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new SerenIT().serializationSpeed();
    }

}
