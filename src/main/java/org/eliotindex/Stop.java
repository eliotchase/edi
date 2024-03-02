package org.eliotindex;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Anything and everything a stop has.
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
class Stop {
	/**
	 * Stop agency, only for global stop search
	 */
	private String agency;
	@NonNull
	private String id;
	@NonNull
	private String name;
	@NonNull
	private double lat;
	@NonNull
	private double lon;
	private String line;
	private int order;
}