package com.indona.invento.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRolesDto {

	@NotNull
	private Long id;

	@NotNull
	private String name;
	
	public List<LabelValuePair> toNameValuePairList() {
        List<LabelValuePair> pairList = new ArrayList<>();
        pairList.add(new LabelValuePair(name, String.valueOf(id)));
        return pairList;
    }

	@Data
    public static class LabelValuePair {
        private String label;
        private String value;

        public LabelValuePair(String label, String value) {
            this.label = label;
            this.value = value;
        }

    }
}
