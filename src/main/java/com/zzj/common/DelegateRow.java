package com.zzj.common;

import io.vertx.mutiny.sqlclient.Row;

import java.time.LocalDateTime;

public class DelegateRow {

    public static final String ID = "id";
    public static final String VER = "version";
    public static final String CTIME = "create_time";
    public static final String UTIME = "update_time";

    private Row row;

    public DelegateRow(Row row) {
        this.row = row;
    }

    public Row getRow() {
        return row;
    }

    public long getId() {
        return getCol(ID);
    }

    public long getVersion() {
        return getCol(VER);
    }

    public long getCol(String col) {
        return hasColumn(col) ? row.getLong(col) : -1;
    }

    public int getIntCol(String col) {
        return hasColumn(col) ? row.getInteger(col) : -1;
    }

    public LocalDateTime getCreateTime() {

        return getTime(CTIME);
    }

    public LocalDateTime getUpdateTime() {
        return getTime(UTIME);
    }

    public LocalDateTime getTime(String col) {
        return hasColumn(col) ? row.getLocalDateTime(col) : null;
    }

    public String getValue(String col) {
        return hasColumn(col) ? row.getString(col) : "";
    }

    public boolean hasColumn(String column) {
        return row.getColumnIndex(column) != -1;
    }
}
