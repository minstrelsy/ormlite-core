package com.j256.ormlite.stmt.mapped;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.SQLException;
import java.util.Iterator;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.PostgresDatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.support.JdbcTemplate;
import com.j256.ormlite.table.TableInfo;

public class MappedCreateTest extends BaseOrmLiteTest {

	@Test
	public void testGeneratedIdSequence() throws Exception {
		databaseType = new PostgresDatabaseType();

		TableInfo<GeneratedId> tableInfo = new TableInfo<GeneratedId>(databaseType, GeneratedId.class);
		StatementExecutor<GeneratedId, String> se = new StatementExecutor<GeneratedId, String>(databaseType, tableInfo);
		JdbcTemplate template = createMock(JdbcTemplate.class);
		expect(template.queryForLong(isA(String.class))).andReturn(1L);
		expect(template.update(isA(String.class), isA(Object[].class), isA(int[].class))).andReturn(1);

		replay(template);
		GeneratedId genIdSeq = new GeneratedId();
		se.create(template, genIdSeq);
		verify(template);
	}

	@Test
	public void testGeneratedIdSequenceLong() throws Exception {
		databaseType = new PostgresDatabaseType();

		StatementExecutor<GeneratedIdLong, String> se =
				new StatementExecutor<GeneratedIdLong, String>(databaseType, new TableInfo<GeneratedIdLong>(
						databaseType, GeneratedIdLong.class));
		JdbcTemplate template = createMock(JdbcTemplate.class);
		expect(template.queryForLong(isA(String.class))).andReturn(1L);
		expect(template.update(isA(String.class), isA(Object[].class), isA(int[].class))).andReturn(1);

		replay(template);
		GeneratedIdLong genIdSeq = new GeneratedIdLong();
		se.create(template, genIdSeq);
		verify(template);
	}

	@Test
	public void testCreateReserverdFields() throws Exception {
		Dao<ReservedField, Object> reservedDao = createDao(ReservedField.class, true);
		String from = "from-string";
		ReservedField res = new ReservedField();
		res.from = from;
		reservedDao.create(res);
		int id = res.select;
		ReservedField res2 = reservedDao.queryForId(id);
		assertEquals(id, res2.select);
		String group = "group-string";
		for (ReservedField reserved : reservedDao) {
			assertEquals(from, reserved.from);
			reserved.group = group;
			reservedDao.update(reserved);
		}
		Iterator<ReservedField> reservedIterator = reservedDao.iterator();
		while (reservedIterator.hasNext()) {
			ReservedField reserved = reservedIterator.next();
			assertEquals(from, reserved.from);
			assertEquals(group, reserved.group);
			reservedIterator.remove();
		}
		assertEquals(0, reservedDao.queryForAll().size());
	}

	@Test
	public void testCreateReserverdTable() throws Exception {
		Dao<Where, String> whereDao = createDao(Where.class, true);
		String id = "from-string";
		Where where = new Where();
		where.id = id;
		whereDao.create(where);
		Where where2 = whereDao.queryForId(id);
		assertEquals(id, where2.id);
		assertEquals(1, whereDao.delete(where2));
		assertNull(whereDao.queryForId(id));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testJustIdInsert() throws Exception {
		createDao(JustId.class, true);
	}

	@Test
	public void testNoCreateSequence() throws Exception {
		MappedCreate.build(databaseType, new TableInfo<GeneratedId>(databaseType, GeneratedId.class));
	}

	@Test
	public void testCreateWithJustGeneratedId() throws Exception {
		Dao<GeneratedId, Integer> generatedIdDao = createDao(GeneratedId.class, true);
		GeneratedId genId = new GeneratedId();
		generatedIdDao.create(genId);
		GeneratedId genId2 = generatedIdDao.queryForId(genId.genId);
		assertEquals(genId.genId, genId2.genId);
	}

	@Test(expected = SQLException.class)
	public void testSequenceZero() throws Exception {
		JdbcTemplate template = createMock(JdbcTemplate.class);
		expect(template.queryForLong(isA(String.class))).andReturn(0L);
		replay(template);
		MappedCreate<GeneratedIdSequence> mappedCreate =
				MappedCreate.build(databaseType, new TableInfo<GeneratedIdSequence>(databaseType,
						GeneratedIdSequence.class));
		mappedCreate.execute(template, new GeneratedIdSequence());
		verify(template);
	}

	private static class GeneratedId {
		@DatabaseField(generatedId = true)
		public int genId;
		@SuppressWarnings("unused")
		@DatabaseField
		public String stuff;
	}

	protected static class GeneratedIdLong {
		@DatabaseField(generatedId = true)
		long id;
		@DatabaseField
		public String stuff;
	}

	protected static class GeneratedIdSequence {
		@DatabaseField(generatedIdSequence = "seq")
		int id;
		@DatabaseField
		public String stuff;
	}

	// for testing reserved words as field names
	protected static class ReservedField {
		@DatabaseField(generatedId = true)
		public int select;
		@DatabaseField
		public String from;
		@DatabaseField
		public String table;
		@DatabaseField
		public String where;
		@DatabaseField
		public String group;
		@DatabaseField
		public String order;
		@DatabaseField
		public String values;
	}

	// for testing reserved table names as fields
	private static class Where {
		@DatabaseField(id = true)
		public String id;
	}

	protected static class JustId {
		@DatabaseField(generatedId = true)
		int id;
	}
}