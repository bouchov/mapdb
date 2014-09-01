package org.mapdb;

import junit.framework.TestCase;

import java.io.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Alexandre Y. Bouchov
 * Date: 27.08.14
 * Time: 11:04
 */
public class SerializerPojoTest2 extends TestCase {
    SerializerPojo p = new SerializerPojo(new CopyOnWriteArrayList<SerializerPojo.ClassInfo>());

    public static class NotSerializableClass {
    }

    public static class Pojo2 implements Serializable {
        private Class classVal;

        public Pojo2(Class classVal) {
            this.classVal = classVal;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pojo2 pojo2 = (Pojo2) o;
            return classVal.equals(pojo2.classVal);
        }

        @Override
        public int hashCode() {
            return classVal.hashCode();
        }
    }

    public static class Pojo implements Serializable {
        private static final int VERSION = 0;
        private transient Pojo2 pojo2Val;

        public Pojo(Pojo2 pojo2Val) {
            this.pojo2Val = pojo2Val;
        }

        private void writeObject(ObjectOutputStream out)
                throws IOException {
            out.defaultWriteObject();
            out.writeInt(VERSION);
            out.writeObject(pojo2Val);
        }

        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            int ver = in.readInt();
            if (ver == 0) {
                pojo2Val = (Pojo2) in.readObject();
            } else {
                throw new IOException("Unknown class version (" + ver + ')');
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pojo pojo = (Pojo) o;

            return pojo2Val.equals(pojo.pojo2Val);
        }

        @Override
        public int hashCode() {
            return pojo2Val.hashCode();
        }
    }

    public static class SimplePojo implements Serializable {
        private Pojo2 pojo2Val;

        public SimplePojo(Pojo2 pojo2Val) {
            this.pojo2Val = pojo2Val;
        }

        public Pojo2 getPojo2Val() {
            return pojo2Val;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SimplePojo that = (SimplePojo) o;

            return pojo2Val.equals(that.pojo2Val);
        }

        @Override
        public int hashCode() {
            return pojo2Val.hashCode();
        }
    }

    public void test_Pojo2() throws Exception {
        serialize(new Pojo2(NotSerializableClass.class));
    }

    public void test_SimplePojo() throws Exception {
        serialize(new SimplePojo(new Pojo2(NotSerializableClass.class)));
    }

    public void test_Pojo() throws Exception {
        serialize(new Pojo(new Pojo2(NotSerializableClass.class)));
    }

    private void serialize(Object i) throws IOException {
        DataOutput2 in = new DataOutput2();
        p.serialize(in, i);
        Object j = p.deserialize(new DataInput2(in.buf), 1);
        assertEquals(i,j);
    }
}
