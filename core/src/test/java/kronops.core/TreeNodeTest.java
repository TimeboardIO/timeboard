package kronops.core;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 Kronops
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import kronops.core.api.TreeNode;
import kronops.core.model.ProjectCluster;
import org.junit.jupiter.api.Test;

public class TreeNodeTest {

    @Test
    public void toStringTest(){


        ProjectCluster p1 = new ProjectCluster();
        p1.setId(10);
        p1.setName("P1");
        ProjectCluster p2 = new ProjectCluster();
        p2.setId(12);
        p2.setName("P2");
        ProjectCluster p12 = new ProjectCluster();
        p12.setId(14);
        p12.setName("P1.2");
        ProjectCluster p13 = new ProjectCluster();
        p13.setId(15);
        p13.setName("P1.3");


        TreeNode root = new TreeNode(null);
        TreeNode cluster1 = new TreeNode(p1);
        TreeNode cluster2 = new TreeNode(p2);
        TreeNode cluster12 = new TreeNode(p12);
        TreeNode cluster13 = new TreeNode(p13);

        root.getChildren().add(cluster1);
        root.getChildren().add(cluster2);
        cluster1.getChildren().add(cluster12);
        cluster1.getChildren().add(cluster13);


        System.out.println(root.getPaths());

    }

}
