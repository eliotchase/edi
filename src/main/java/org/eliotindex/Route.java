package org.eliotindex;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class Route {
	private String lineCode;
	private double edi;
	private String lineName;
	private String branch;
}
