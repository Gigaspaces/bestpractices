import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class TestParsing {
    @Test
    public void testParsing() {
        double t=Double.parseDouble("-102.54");
        assertTrue(t != 0);
    }
}
