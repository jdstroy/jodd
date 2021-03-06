// Copyright (c) 2003-2014, Jodd Team (jodd.org). All Rights Reserved.

package jodd.db.oom.sqlgen.chunks;

import jodd.db.oom.DbEntityDescriptor;
import jodd.db.oom.DbEntityColumnDescriptor;
import jodd.db.oom.sqlgen.DbSqlBuilderException;
import jodd.util.StringUtil;
import jodd.bean.BeanUtil;

/**
 * Renders condition part of the sql query based on values in provided entity object.
 * Conditions are defined by following expression: <code>tableRef = objectRef</code>.
 * Matching may be done for existing columns (non-null), all columns (including nulls)
 * and just for the identity columns.
 */
public class MatchChunk extends SqlChunk {

	private static final String AND = " and ";
	private static final String DEFAULT = "1=1";
	private static final String DOT_STAR = ".*";
	private static final String DOT_PLUS = ".+";
	
	protected Object data;
	protected final String tableRef;
	protected final String objectRef;
	protected final int includeColumns;

	public MatchChunk(String tableRef, Object data, int includeColumns) {
		this(tableRef, null, data, includeColumns);
	}

	public MatchChunk(String tableRef, String objectRef, int includeColumns) {
		this(tableRef, objectRef, null, includeColumns);
	}

	protected MatchChunk(String tableRef, String objectRef, Object data, int includeColumns) {
		super(CHUNK_MATCH);
		this.tableRef = tableRef;
		this.objectRef = objectRef;
		this.data = data;
		this.includeColumns = includeColumns;
	}

	public MatchChunk(String expression) {
		super(CHUNK_MATCH);
		expression = expression.trim();
		int lastNdx = expression.length();
		if (expression.endsWith(DOT_STAR)) {
			lastNdx -= 2;
			includeColumns = COLS_ALL;
		} else if (expression.endsWith(DOT_PLUS)) {
			lastNdx -= 2;
			includeColumns = COLS_ONLY_IDS;
		} else {
			includeColumns = COLS_ONLY_EXISTING;
		}
		int eq = expression.indexOf('=');
		if (eq == -1) {
			throw new DbSqlBuilderException("Template syntax error, expected 'match' equality: {tableRef=objectRef}.");
		}
		tableRef = expression.substring(0, eq).trim();
		objectRef = expression.substring(eq + 1, lastNdx).trim();
	}

	@Override
	public void process(StringBuilder out) {
		if (objectRef != null) {
			data = templateData.lookupObject(objectRef);
		}
		DbEntityDescriptor ded = lookupTableRef(tableRef);
		String table = resolveTable(tableRef, ded);
		DbEntityColumnDescriptor[] decList = ded.getColumnDescriptors();
		String typeName = StringUtil.uncapitalize(ded.getEntityName());

		int count = 0;
		out.append('(');
		for (DbEntityColumnDescriptor dec : decList) {
			if ((includeColumns == COLS_ONLY_IDS) && (dec.isId() == false)) {
				continue;
			}
			String property = dec.getPropertyName();

			Object value = BeanUtil.getDeclaredPropertySilently(data, property);

			if ((includeColumns == COLS_ONLY_EXISTING) && (value == null)) {
				continue;
			}

			if (includeColumns == COLS_ONLY_EXISTING) {
				if (value == null) {
					continue;
				}
				// special case for ID column
				if (dec.isId() && value instanceof Number) {
					if (((Number) value).longValue() == 0) {
						continue;
					}
				}
			}
			if (count > 0) {
				out.append(AND);
			}
			count++;
			out.append(table).append('.').append(dec.getColumnName()).append('=');
			String propertyName = typeName + '.' + property;
			defineParameter(out, propertyName, value, dec);
		}
		if (count == 0) {
			out.append(DEFAULT);
		}
		out.append(')');
	}

}