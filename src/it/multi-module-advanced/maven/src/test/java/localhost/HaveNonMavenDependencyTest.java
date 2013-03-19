package localhost;

import junit.framework.Assert;
import org.junit.*;

import java.lang.Exception;

public class HaveNonMavenDependencyTest {

    @Test
    public void haveLicenseDotTxt() throws Exception {
        Assert.assertNotNull("The '/LICENSE.txt' resource is on the classpath via the non-maven dependency",
                getClass().getResource("/LICENSE.txt"));
    }
}