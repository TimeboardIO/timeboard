package kronops.core.api;

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

import kronops.core.model.ProjectCluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeNode {

    private ProjectCluster projectCluster;
    private List<TreeNode> children = new ArrayList<>();

    public TreeNode(ProjectCluster projectCluster) {
        this.projectCluster = projectCluster;
    }


    public Map<Long, String> getPaths() {
        Map<Long, String> cPath = new HashMap<>();
        if (this.projectCluster == null) {
            this.projectCluster = new ProjectCluster();
            this.projectCluster.setName("Root");
        }
        cPath.put(this.projectCluster.getId(), this.projectCluster.getName() + " > ");
        StringBuilder buff = new StringBuilder();
        buff.append(this.projectCluster.getName());

        buff.append(" > ");


        this.getChildren().forEach(treeNode -> {
            getPathRec(cPath, buff, treeNode);
        });


        cPath.forEach((aLong, s) -> {
            s.substring(0, s.length() - 1);
        });

        return cPath;
    }

    private void getPathRec(Map<Long, String> cPath, StringBuilder buff, TreeNode treeNode) {
        StringBuilder localBuffer = new StringBuilder();
        localBuffer.append(treeNode.getProjectCluster().getName());
        localBuffer.append(" > ");
        cPath.put(treeNode.getProjectCluster().getId(), buff.toString() + localBuffer.toString());
        if (!treeNode.getChildren().isEmpty()) {
            treeNode.getChildren().forEach(treeNode1 -> {
                getPathRec(cPath, new StringBuilder(buff.toString() + localBuffer.toString()), treeNode1);
            });
        }
    }


    public void insert(ProjectCluster projectCluster) {
        if (projectCluster.getParent() == null) {
            this.children.add(new TreeNode(projectCluster));
        } else {
            this.insertRec(this, projectCluster);
        }
    }

    private void insertRec(TreeNode node, ProjectCluster projectCluster) {
        this.children.forEach(treeNode -> {
            if (treeNode.getProjectCluster().getId() == projectCluster.getParent().getId()) {
                treeNode.getChildren().add(new TreeNode(projectCluster));
            } else {
                insertRec(treeNode, projectCluster);
            }
        });
    }

    public ProjectCluster getProjectCluster() {
        return projectCluster;
    }

    public void setProjectCluster(ProjectCluster projectCluster) {
        this.projectCluster = projectCluster;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }
}
