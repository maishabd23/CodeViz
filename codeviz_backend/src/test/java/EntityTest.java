import codeViz.entity.ClassEntity;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.*;

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
}
