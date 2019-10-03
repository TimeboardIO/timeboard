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

/**
 * Represent a tree of ProjectClusters.
 */
public final class TreeNode {

    /**
     * Childre of current node.
     */
    private final List<TreeNode> children = new ArrayList<>();
    /**
     * Node payload : current project cluster.
     */
    private ProjectCluster projectCluster;

    /**
     * TreeNode constructor.
     *
     * @param pc payload of current TreeNode
     */
    public TreeNode(final ProjectCluster pc) {
        this.projectCluster = pc;
    }

    /**
     * Flat representation of tree.
     *
     * @return key : projectCluster ID, value : path to node
     */
    public Map<Long, String> getPaths() {
        Map<Long, String> cPath = new HashMap<>();
        if (this.projectCluster == null) {
            this.projectCluster = new ProjectCluster();
            this.projectCluster.setName("Root");
        }
        cPath.put(this.projectCluster.getId(),
                this.projectCluster.getName() + " > ");
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

    private void getPathRec(final Map<Long, String> cPath,
                            final StringBuilder buff,
                            final TreeNode treeNode) {
        StringBuilder localBuffer = new StringBuilder();
        localBuffer.append(treeNode.getProjectCluster().getName());
        localBuffer.append(" > ");
        cPath.put(treeNode.getProjectCluster().getId(),
                buff.toString() + localBuffer.toString());
        if (!treeNode.getChildren().isEmpty()) {
            treeNode.getChildren().forEach(treeNode1 -> {
                getPathRec(cPath, new StringBuilder(buff.toString()
                        + localBuffer.toString()), treeNode1);
            });
        }
    }


    /**
     * Insert children to current node.
     *
     * @param pc payload to insert as a child of current node
     */
    public void insert(final ProjectCluster pc) {
        if (pc.getParent() == null) {
            this.children.add(new TreeNode(pc));
        } else {
            this.insertRec(this, pc);
        }
    }

    /**
     * used to find and insert ProjectCluster at the right position in tree.
     *
     * @param node current node
     * @param pc   ProjectCluster to insert
     */
    private void insertRec(final TreeNode node, final ProjectCluster pc) {
        node.children.forEach(treeNode -> {
            if (treeNode.getProjectCluster().getId()
                    == pc.getParent().getId()) {
                treeNode.getChildren().add(new TreeNode(pc));
            } else {
                insertRec(treeNode, pc);
            }
        });
    }

    /**
     * @return node payload
     */
    public ProjectCluster getProjectCluster() {
        return projectCluster;
    }

    /**
     * set payload.
     *
     * @param pc payload
     */
    public void setProjectCluster(final ProjectCluster pc) {
        this.projectCluster = pc;
    }

    /**
     * @return Current node children. May be empty list.
     */
    public List<TreeNode> getChildren() {
        return children;
    }


}
