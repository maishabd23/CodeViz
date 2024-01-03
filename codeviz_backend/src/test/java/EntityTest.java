import codeViz.entity.ClassEntity;
import org.junit.Test;
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
        System.out.println(classEntity.getColour());

        assertFalse(classEntity.getColour().getRed() > 240 && classEntity.getColour().getGreen() > 240 && classEntity.getColour().getBlue() < 150);
    }
}
