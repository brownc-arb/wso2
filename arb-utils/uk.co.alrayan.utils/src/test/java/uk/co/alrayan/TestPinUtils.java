package uk.co.alrayan;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import uk.co.alrayan.PinUtils;

public class TestPinUtils {

  /*
  public static void main(String[] args) {

    TestPinUtils tpu = new TestPinUtils();
    tpu.test();
  }
*/
  @Test
  public void test() {

    int ret =  0;

    ret = PinUtils.checkPin("111111");
    assertEquals("11111 test fail", -2, ret);

    ret = PinUtils.checkPin("222222");
    assertEquals("222222 test fail", -2, ret);

    ret = PinUtils.checkPin("222223");
    assertEquals("222223 test fail", -2, ret);
    ret = PinUtils.checkPin("222322");
    assertEquals("222322 test fail", -2, ret);

    ret = PinUtils.checkPin("123456");
    assertEquals("123456 test fail", -3, ret);
    ret = PinUtils.checkPin("456789");
    assertEquals("456789 test fail", -3, ret);
    ret = PinUtils.checkPin("987654");
    assertEquals("987654 test fail", -3, ret);
    ret = PinUtils.checkPin("654321");
    assertEquals("654321 test fail", -3, ret);
    ret = PinUtils.checkPin("");
    assertEquals("empty test fail", -1, ret);

    ret = PinUtils.checkPin("123");
    assertEquals("123 test fail", -1, ret);

    // these succeed
    ret = PinUtils.checkPin("213431");
    assertEquals("213431 test fail", 0, ret);
    ret = PinUtils.checkPin("674323");
    assertEquals("674323 test fail", 0, ret);


    System.out.println("ret=" + ret);

  }
}
