package com.jimilab.uwclient.gen;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.jimilab.uwclient.dao.MaterialDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "MATERIAL_DAO".
*/
public class MaterialDaoDao extends AbstractDao<MaterialDao, Long> {

    public static final String TABLENAME = "MATERIAL_DAO";

    /**
     * Properties of entity MaterialDao.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property TaskName = new Property(1, String.class, "taskName", false, "TASK_NAME");
        public final static Property Destination = new Property(2, String.class, "destination", false, "DESTINATION");
        public final static Property No = new Property(3, String.class, "no", false, "NO");
        public final static Property TaskSupplier = new Property(4, String.class, "taskSupplier", false, "TASK_SUPPLIER");
        public final static Property PlanQuantity = new Property(5, int.class, "planQuantity", false, "PLAN_QUANTITY");
        public final static Property MaterialId = new Property(6, String.class, "materialId", false, "MATERIAL_ID");
        public final static Property MaterialSupplier = new Property(7, String.class, "materialSupplier", false, "MATERIAL_SUPPLIER");
        public final static Property Quantity = new Property(8, int.class, "quantity", false, "QUANTITY");
        public final static Property ProductionTime = new Property(9, String.class, "productionTime", false, "PRODUCTION_TIME");
        public final static Property BoxId = new Property(10, int.class, "boxId", false, "BOX_ID");
        public final static Property OperateType = new Property(11, int.class, "operateType", false, "OPERATE_TYPE");
        public final static Property Operator = new Property(12, String.class, "operator", false, "OPERATOR");
    }


    public MaterialDaoDao(DaoConfig config) {
        super(config);
    }
    
    public MaterialDaoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"MATERIAL_DAO\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"TASK_NAME\" TEXT," + // 1: taskName
                "\"DESTINATION\" TEXT," + // 2: destination
                "\"NO\" TEXT," + // 3: no
                "\"TASK_SUPPLIER\" TEXT," + // 4: taskSupplier
                "\"PLAN_QUANTITY\" INTEGER NOT NULL ," + // 5: planQuantity
                "\"MATERIAL_ID\" TEXT," + // 6: materialId
                "\"MATERIAL_SUPPLIER\" TEXT," + // 7: materialSupplier
                "\"QUANTITY\" INTEGER NOT NULL ," + // 8: quantity
                "\"PRODUCTION_TIME\" TEXT," + // 9: productionTime
                "\"BOX_ID\" INTEGER NOT NULL ," + // 10: boxId
                "\"OPERATE_TYPE\" INTEGER NOT NULL ," + // 11: operateType
                "\"OPERATOR\" TEXT);"); // 12: operator
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"MATERIAL_DAO\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, MaterialDao entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String taskName = entity.getTaskName();
        if (taskName != null) {
            stmt.bindString(2, taskName);
        }
 
        String destination = entity.getDestination();
        if (destination != null) {
            stmt.bindString(3, destination);
        }
 
        String no = entity.getNo();
        if (no != null) {
            stmt.bindString(4, no);
        }
 
        String taskSupplier = entity.getTaskSupplier();
        if (taskSupplier != null) {
            stmt.bindString(5, taskSupplier);
        }
        stmt.bindLong(6, entity.getPlanQuantity());
 
        String materialId = entity.getMaterialId();
        if (materialId != null) {
            stmt.bindString(7, materialId);
        }
 
        String materialSupplier = entity.getMaterialSupplier();
        if (materialSupplier != null) {
            stmt.bindString(8, materialSupplier);
        }
        stmt.bindLong(9, entity.getQuantity());
 
        String productionTime = entity.getProductionTime();
        if (productionTime != null) {
            stmt.bindString(10, productionTime);
        }
        stmt.bindLong(11, entity.getBoxId());
        stmt.bindLong(12, entity.getOperateType());
 
        String operator = entity.getOperator();
        if (operator != null) {
            stmt.bindString(13, operator);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, MaterialDao entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String taskName = entity.getTaskName();
        if (taskName != null) {
            stmt.bindString(2, taskName);
        }
 
        String destination = entity.getDestination();
        if (destination != null) {
            stmt.bindString(3, destination);
        }
 
        String no = entity.getNo();
        if (no != null) {
            stmt.bindString(4, no);
        }
 
        String taskSupplier = entity.getTaskSupplier();
        if (taskSupplier != null) {
            stmt.bindString(5, taskSupplier);
        }
        stmt.bindLong(6, entity.getPlanQuantity());
 
        String materialId = entity.getMaterialId();
        if (materialId != null) {
            stmt.bindString(7, materialId);
        }
 
        String materialSupplier = entity.getMaterialSupplier();
        if (materialSupplier != null) {
            stmt.bindString(8, materialSupplier);
        }
        stmt.bindLong(9, entity.getQuantity());
 
        String productionTime = entity.getProductionTime();
        if (productionTime != null) {
            stmt.bindString(10, productionTime);
        }
        stmt.bindLong(11, entity.getBoxId());
        stmt.bindLong(12, entity.getOperateType());
 
        String operator = entity.getOperator();
        if (operator != null) {
            stmt.bindString(13, operator);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public MaterialDao readEntity(Cursor cursor, int offset) {
        MaterialDao entity = new MaterialDao( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // taskName
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // destination
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // no
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // taskSupplier
            cursor.getInt(offset + 5), // planQuantity
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // materialId
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // materialSupplier
            cursor.getInt(offset + 8), // quantity
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // productionTime
            cursor.getInt(offset + 10), // boxId
            cursor.getInt(offset + 11), // operateType
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12) // operator
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, MaterialDao entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTaskName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setDestination(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setNo(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setTaskSupplier(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setPlanQuantity(cursor.getInt(offset + 5));
        entity.setMaterialId(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setMaterialSupplier(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setQuantity(cursor.getInt(offset + 8));
        entity.setProductionTime(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setBoxId(cursor.getInt(offset + 10));
        entity.setOperateType(cursor.getInt(offset + 11));
        entity.setOperator(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(MaterialDao entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(MaterialDao entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(MaterialDao entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
