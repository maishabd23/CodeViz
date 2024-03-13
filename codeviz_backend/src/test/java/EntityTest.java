import codeViz.entity.ClassEntity;
import codeViz.entity.MethodEntity;
import codeViz.entity.PackageEntity;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thanuja Sivaananthan
 */
public class EntityTest {


    /**
     * Test that default colours are not too similar to highlighted colour
     */
    @Test
    public void testGetRandomColour() {
        ClassEntity classEntity = new ClassEntity("Entity");
        Color color = classEntity.getParentColour();
        System.out.println(color);

        assertFalse(color.getRed() > 240 && color.getGreen() > 240 && color.getBlue() < 150);
    }


    /**
     * Test the package size is set as expected
     */
    @Test
    public void testPackageSize() {
        int size_inc = PackageEntity.getSizeIncrement();
        PackageEntity packageEntity1, packageEntity2, packageEntity3;
        ClassEntity classEntity1a, classEntity2a, classEntity2b, classEntity3a;

        packageEntity1 = new PackageEntity("package1");
        packageEntity2 = new PackageEntity("package2", packageEntity1);
        packageEntity3 = new PackageEntity("package3", packageEntity1);

        // initial size based on subpackages
        assertEquals((1+2)*size_inc, packageEntity1.getSize());
        assertEquals(size_inc, packageEntity2.getSize());
        assertEquals(size_inc, packageEntity3.getSize());

        // adding connections does not change the size
        packageEntity2.addConnectedEntity(packageEntity3);
        assertEquals(size_inc, packageEntity2.getSize());
        assertEquals(size_inc, packageEntity3.getSize());

        classEntity1a = new ClassEntity("class1a", packageEntity1);
        classEntity2a = new ClassEntity("class2a", packageEntity2);
        classEntity2b = new ClassEntity("class2b", packageEntity2);
        classEntity3a = new ClassEntity("class1a", packageEntity3);

        // size based on classes in each package
        assertEquals((1+2+1)*size_inc, packageEntity1.getSize());
        assertEquals((1+2)*size_inc, packageEntity2.getSize());
        assertEquals((1+1)*size_inc, packageEntity3.getSize());
    }

    /**
     * Test the class size is set as expected
     */
    @Test
    public void testClassSize() {
        int size_inc = ClassEntity.getSizeIncrement();
        ClassEntity classEntity1, classEntity1a;
        MethodEntity methodEntity1, methodEntity1a1, methodEntity1a2;

        classEntity1 = new ClassEntity("class1");
        classEntity1a = new ClassEntity("class1a");
        classEntity1a.setSuperClass(classEntity1);

        // size based on class hierarchy
        assertEquals((1+1)*size_inc, classEntity1.getSize());
        assertEquals(size_inc, classEntity1a.getSize());

        classEntity1.addField(new ClassEntity("String"));
        classEntity1.addField(classEntity1a);

        methodEntity1 = new MethodEntity("method1", classEntity1);
        methodEntity1a1 = new MethodEntity("method1a1", classEntity1a);
        methodEntity1a2 = new MethodEntity("method1a2", classEntity1a);

        // size based on fields and methods
        assertEquals((1+1+2+1)*size_inc, classEntity1.getSize());
        assertEquals((1+2)*size_inc, classEntity1a.getSize());
    }


    /**
     * Test the node size is set as expected
     */
    @Test
    public void testNodeSize() {
        int size_inc = MethodEntity.getSizeIncrement();
        ClassEntity classEntity1, classEntity1a;
        MethodEntity methodEntity1, methodEntity1a1, methodEntity1a2;

        classEntity1 = new ClassEntity("class1");
        classEntity1a = new ClassEntity("class1a");
        classEntity1a.setSuperClass(classEntity1);

        methodEntity1 = new MethodEntity("method1", classEntity1);
        methodEntity1a1 = new MethodEntity("method1a1", classEntity1a);
        methodEntity1a2 = new MethodEntity("method1a2", classEntity1a);

        assertEquals(size_inc, methodEntity1.getSize());
        assertEquals(size_inc, methodEntity1a1.getSize());
        assertEquals(size_inc, methodEntity1a2.getSize());

        methodEntity1.addArgument(classEntity1a);
        methodEntity1.addArgument(classEntity1);
        methodEntity1.setReturnType(new ClassEntity("String"));

        methodEntity1a1.addArgument(classEntity1a);
        methodEntity1a1.addArgument(classEntity1);
        methodEntity1a1.setReturnType(new ClassEntity("void"));

        methodEntity1a2.addArgument(new ClassEntity("Color"));
        methodEntity1a2.setReturnType(classEntity1);

        // size based on arguments and return type
        assertEquals((1+3)*size_inc, methodEntity1.getSize());
        assertEquals((1+2)*size_inc, methodEntity1a1.getSize());
        assertEquals((1+2)*size_inc, methodEntity1a2.getSize());
    }
}
