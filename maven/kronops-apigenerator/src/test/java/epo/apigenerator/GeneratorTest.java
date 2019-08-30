package kronops.apigenerator;


import kronops.test.AnotherDemo;
import org.junit.Test;

public class GeneratorTest {

    @Test
    public void testGenerate() throws Exception {
        Generator.run(GeneratorTest.class.getClassLoader(),"kronops", "target/api.ts");
    }

}
