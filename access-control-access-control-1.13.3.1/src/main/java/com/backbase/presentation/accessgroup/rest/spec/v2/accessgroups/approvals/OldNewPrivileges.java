
package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import com.backbase.buildingblocks.persistence.model.AdditionalPropertiesAware;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Presentation action
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "view",
    "create",
    "edit",
    "delete",
    "approve",
    "cancel",
    "execute"
})
public class OldNewPrivileges implements AdditionalPropertiesAware
{

    @JsonProperty("view")
    @Valid
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.View view;
    @JsonProperty("create")
    @Valid
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Create create;
    @JsonProperty("edit")
    @Valid
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Edit edit;
    @JsonProperty("delete")
    @Valid
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Delete delete;
    @JsonProperty("approve")
    @Valid
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Approve approve;
    @JsonProperty("cancel")
    @Valid
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Cancel cancel;
    @JsonProperty("execute")
    @Valid
    private com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Execute execute;
    /**
     * Additional Properties
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> additions = new HashMap<String, String>();

    /**
     * 
     * @return
     *     The view
     */
    @JsonProperty("view")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.View getView() {
        return view;
    }

    /**
     * 
     * @param view
     *     The view
     */
    @JsonProperty("view")
    public void setView(com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.View view) {
        this.view = view;
    }

    public OldNewPrivileges withView(View view) {
        this.view = view;
        return this;
    }

    /**
     * 
     * @return
     *     The create
     */
    @JsonProperty("create")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Create getCreate() {
        return create;
    }

    /**
     * 
     * @param create
     *     The create
     */
    @JsonProperty("create")
    public void setCreate(com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Create create) {
        this.create = create;
    }

    public OldNewPrivileges withCreate(Create create) {
        this.create = create;
        return this;
    }

    /**
     * 
     * @return
     *     The edit
     */
    @JsonProperty("edit")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Edit getEdit() {
        return edit;
    }

    /**
     * 
     * @param edit
     *     The edit
     */
    @JsonProperty("edit")
    public void setEdit(com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Edit edit) {
        this.edit = edit;
    }

    public OldNewPrivileges withEdit(Edit edit) {
        this.edit = edit;
        return this;
    }

    /**
     * 
     * @return
     *     The delete
     */
    @JsonProperty("delete")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Delete getDelete() {
        return delete;
    }

    /**
     * 
     * @param delete
     *     The delete
     */
    @JsonProperty("delete")
    public void setDelete(com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Delete delete) {
        this.delete = delete;
    }

    public OldNewPrivileges withDelete(Delete delete) {
        this.delete = delete;
        return this;
    }

    /**
     * 
     * @return
     *     The approve
     */
    @JsonProperty("approve")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Approve getApprove() {
        return approve;
    }

    /**
     * 
     * @param approve
     *     The approve
     */
    @JsonProperty("approve")
    public void setApprove(com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Approve approve) {
        this.approve = approve;
    }

    public OldNewPrivileges withApprove(Approve approve) {
        this.approve = approve;
        return this;
    }

    /**
     * 
     * @return
     *     The cancel
     */
    @JsonProperty("cancel")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Cancel getCancel() {
        return cancel;
    }

    /**
     * 
     * @param cancel
     *     The cancel
     */
    @JsonProperty("cancel")
    public void setCancel(com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Cancel cancel) {
        this.cancel = cancel;
    }

    public OldNewPrivileges withCancel(Cancel cancel) {
        this.cancel = cancel;
        return this;
    }

    /**
     * 
     * @return
     *     The execute
     */
    @JsonProperty("execute")
    public com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Execute getExecute() {
        return execute;
    }

    /**
     * 
     * @param execute
     *     The execute
     */
    @JsonProperty("execute")
    public void setExecute(com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Execute execute) {
        this.execute = execute;
    }

    public OldNewPrivileges withExecute(Execute execute) {
        this.execute = execute;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(view).append(create).append(edit).append(delete).append(approve).append(cancel).append(execute).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OldNewPrivileges) == false) {
            return false;
        }
        OldNewPrivileges rhs = ((OldNewPrivileges) other);
        return new EqualsBuilder().append(view, rhs.view).append(create, rhs.create).append(edit, rhs.edit).append(delete, rhs.delete).append(approve, rhs.approve).append(cancel, rhs.cancel).append(execute, rhs.execute).isEquals();
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    @JsonProperty("additions")
    public Map<String, String> getAdditions() {
        return this.additions;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    @JsonProperty("additions")
    public void setAdditions(Map<String, String> additions) {
        this.additions = additions;
    }

}
