package common.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 变更结果类.
 *
 * @author: pbad
 * @date: 2025-11-27 18:25:59
 * @version: 1.0
 */
public class ChangeResult {
    @JsonProperty("changeBeforeFieldList")
    private List<FieldChangeInfo> changeBeforeFieldList;
    
    @JsonProperty("changeAfterFieldList")
    private List<FieldChangeInfo> changeAfterFieldList;

    public ChangeResult() {
        this.changeBeforeFieldList = new ArrayList<>();
        this.changeAfterFieldList = new ArrayList<>();
    }

    // Getters and Setters
    public List<FieldChangeInfo> getChangeBeforeFieldList() { return changeBeforeFieldList; }
    public void setChangeBeforeFieldList(List<FieldChangeInfo> changeBeforeFieldList) { 
        this.changeBeforeFieldList = changeBeforeFieldList; 
    }
    
    public List<FieldChangeInfo> getChangeAfterFieldList() { return changeAfterFieldList; }
    public void setChangeAfterFieldList(List<FieldChangeInfo> changeAfterFieldList) { 
        this.changeAfterFieldList = changeAfterFieldList; 
    }
}