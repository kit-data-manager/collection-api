/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.kit.datamanager.collection.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * Helper object for local pagination using the Tabulator.js library.
 *
 * @author jejkal
 */
@Getter
@Builder
public class TabulatorLocalPagination {

    @JsonProperty("last_page")
    private int lastPage;

    private List<?> data;
}
