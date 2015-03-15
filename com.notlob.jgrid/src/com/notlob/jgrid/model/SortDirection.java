package com.notlob.jgrid.model;

public enum SortDirection {
	NONE,
	ASC,
	DESC;

	@Override
	public String toString() {
		switch (this) {
			case ASC:  return "Ascending";
			case DESC: return "Descending";
			case NONE: return "None";
		}

		return super.toString();
	}

}
