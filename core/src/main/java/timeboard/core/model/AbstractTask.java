package timeboard.core.model;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 Timeboard
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

import javax.persistence.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractTask extends OrganizationEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 500)
    private String comments;

    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(nullable = false)
    private String origin;

    @Column(nullable = true)
    private String remotePath;

    @Column(nullable = true)
    private String remoteId;


    @OneToMany(targetEntity = Imputation.class,
            mappedBy = "task", fetch = FetchType.EAGER,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<Imputation> imputations;

    public AbstractTask() {
        this.imputations = new HashSet<>();
    }

    public AbstractTask(final String taskName, final String taskComment, final Date startDate, final Date endDate) {
        this.setName(taskName);
        this.setComments(taskComment);
        this.setStartDate(startDate);
        this.setEndDate(endDate);
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public Set<Imputation> getImputations() {
        return imputations;
    }

    public void setImputations(final Set<Imputation> imputations) {
        this.imputations = imputations;
    }


    public String getOrigin() {
        return origin;
    }

    public void setOrigin(final String origin) {
        this.origin = origin.toLowerCase();
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(final String remotePath) {
        this.remotePath = remotePath;
    }

    public String getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(final String remoteId) {
        this.remoteId = remoteId;
    }

    @Transient
    public double findTaskImputationValueByDate(final Date date, final Account account) {
        final Optional<Imputation> imputationOptional = this.getImputations().stream()
                .filter(imputation -> areDateSameDay(date, imputation.getDay()))
                .filter(imputation -> imputation.getAccount().getId() == account.getId())
                .findFirst();
        if (imputationOptional.isPresent()) {
            return imputationOptional.get().getValue();
        } else {
            return 0;
        }
    }

    private boolean areDateSameDay(final Date date1, final Date date2) {
        return new SimpleDateFormat("yyyy-MM-dd")
                .format(date1)
                .equals(new SimpleDateFormat("yyyy-MM-dd").format(date2));
    }


}
