import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleTest {
  @Test
  public void simpleTest() {
    var a = 2;
    assertEquals(4, a + a);
  }
}
