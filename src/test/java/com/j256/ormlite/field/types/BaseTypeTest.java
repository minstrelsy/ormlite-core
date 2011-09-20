package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DataPersister;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;

public abstract class BaseTypeTest extends BaseCoreTest {

	protected static final String TABLE_NAME = "foo";
	protected static final FieldType[] noFieldTypes = new FieldType[0];

	protected void testType(Class<?> clazz, Object javaVal, Object defaultSqlVal, Object sqlArg, String defaultValStr,
			DataType dataType, String columnName, boolean isValidGeneratedType, boolean isAppropriateId,
			boolean isEscapedValue, boolean isPrimitive, boolean isSelectArgRequired, boolean isStreamType,
			boolean isComparable, boolean isConvertableId) throws Exception {
		DataPersister dataPersister = dataType.getDataPersister();
		DatabaseConnection conn = connectionSource.getReadOnlyConnection();
		CompiledStatement stmt = null;
		try {
			stmt = conn.compileStatement("select * from " + TABLE_NAME, StatementType.SELECT, noFieldTypes);
			DatabaseResults results = stmt.runQuery(null);
			assertTrue(results.next());
			int colNum = results.findColumn(columnName);
			FieldType fieldType =
					FieldType.createFieldType(connectionSource, TABLE_NAME, clazz.getDeclaredField(columnName), clazz);
			if (javaVal instanceof byte[]) {
				assertTrue(Arrays.equals((byte[]) javaVal,
						(byte[]) dataPersister.resultToJava(fieldType, results, colNum)));
			} else {
				Map<String, Integer> colMap = new HashMap<String, Integer>();
				colMap.put(columnName, colNum);
				Object result = fieldType.resultToJava(results, colMap);
				assertEquals(javaVal, result);
			}
			if (dataType == DataType.STRING_BYTES || dataType == DataType.BYTE_ARRAY
					|| dataType == DataType.SERIALIZABLE) {
				try {
					dataPersister.parseDefaultString(fieldType, "");
					fail("parseDefaultString should have thrown for " + dataType);
				} catch (SQLException e) {
					// expected
				}
			} else if (defaultValStr != null) {
				assertEquals(defaultSqlVal, dataPersister.parseDefaultString(fieldType, defaultValStr));
			}
			if (sqlArg == null) {
				// noop
			} else if (sqlArg instanceof byte[]) {
				assertTrue(Arrays.equals((byte[]) sqlArg, (byte[]) dataPersister.javaToSqlArg(fieldType, javaVal)));
			} else {
				assertEquals(sqlArg, dataPersister.javaToSqlArg(fieldType, javaVal));
			}
			assertEquals(isValidGeneratedType, dataPersister.isValidGeneratedType());
			assertEquals(isAppropriateId, dataPersister.isAppropriateId());
			assertEquals(isEscapedValue, dataPersister.isEscapedValue());
			assertEquals(isEscapedValue, dataPersister.isEscapedDefaultValue());
			assertEquals(isPrimitive, dataPersister.isPrimitive());
			assertEquals(isSelectArgRequired, dataPersister.isSelectArgRequired());
			assertEquals(isStreamType, dataPersister.isStreamType());
			assertEquals(isComparable, dataPersister.isComparable());
			if (isConvertableId) {
				assertNotNull(dataPersister.convertIdNumber(10));
			} else {
				assertNull(dataPersister.convertIdNumber(10));
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			connectionSource.releaseConnection(conn);
		}
	}

}