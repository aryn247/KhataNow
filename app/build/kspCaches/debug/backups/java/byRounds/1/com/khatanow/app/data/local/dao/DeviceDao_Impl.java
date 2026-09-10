package com.khatanow.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.khatanow.app.data.local.entities.DeviceEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DeviceDao_Impl implements DeviceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DeviceEntity> __insertionAdapterOfDeviceEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateLastSynced;

  private final SharedSQLiteStatement __preparedStmtOfRemoveDevice;

  public DeviceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDeviceEntity = new EntityInsertionAdapter<DeviceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `authorized_devices` (`deviceId`,`deviceName`,`role`,`pairedAt`,`lastSyncedAt`,`isAuthorized`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DeviceEntity entity) {
        statement.bindString(1, entity.getDeviceId());
        statement.bindString(2, entity.getDeviceName());
        statement.bindString(3, entity.getRole());
        statement.bindLong(4, entity.getPairedAt());
        statement.bindLong(5, entity.getLastSyncedAt());
        final int _tmp = entity.isAuthorized() ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    };
    this.__preparedStmtOfUpdateLastSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE authorized_devices SET lastSyncedAt = ? WHERE deviceId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRemoveDevice = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM authorized_devices WHERE deviceId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertDevice(final DeviceEntity device,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDeviceEntity.insert(device);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLastSynced(final String deviceId, final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateLastSynced.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
        _argIndex = 2;
        _stmt.bindString(_argIndex, deviceId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateLastSynced.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object removeDevice(final String deviceId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRemoveDevice.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, deviceId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfRemoveDevice.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DeviceEntity>> getAuthorizedDevices() {
    final String _sql = "SELECT * FROM authorized_devices WHERE isAuthorized = 1 ORDER BY deviceName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"authorized_devices"}, new Callable<List<DeviceEntity>>() {
      @Override
      @NonNull
      public List<DeviceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceName");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfPairedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "pairedAt");
          final int _cursorIndexOfLastSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncedAt");
          final int _cursorIndexOfIsAuthorized = CursorUtil.getColumnIndexOrThrow(_cursor, "isAuthorized");
          final List<DeviceEntity> _result = new ArrayList<DeviceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DeviceEntity _item;
            final String _tmpDeviceId;
            _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            final String _tmpDeviceName;
            _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            final String _tmpRole;
            _tmpRole = _cursor.getString(_cursorIndexOfRole);
            final long _tmpPairedAt;
            _tmpPairedAt = _cursor.getLong(_cursorIndexOfPairedAt);
            final long _tmpLastSyncedAt;
            _tmpLastSyncedAt = _cursor.getLong(_cursorIndexOfLastSyncedAt);
            final boolean _tmpIsAuthorized;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAuthorized);
            _tmpIsAuthorized = _tmp != 0;
            _item = new DeviceEntity(_tmpDeviceId,_tmpDeviceName,_tmpRole,_tmpPairedAt,_tmpLastSyncedAt,_tmpIsAuthorized);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getDeviceById(final String deviceId,
      final Continuation<? super DeviceEntity> $completion) {
    final String _sql = "SELECT * FROM authorized_devices WHERE deviceId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, deviceId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DeviceEntity>() {
      @Override
      @Nullable
      public DeviceEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceName");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfPairedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "pairedAt");
          final int _cursorIndexOfLastSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncedAt");
          final int _cursorIndexOfIsAuthorized = CursorUtil.getColumnIndexOrThrow(_cursor, "isAuthorized");
          final DeviceEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpDeviceId;
            _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            final String _tmpDeviceName;
            _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            final String _tmpRole;
            _tmpRole = _cursor.getString(_cursorIndexOfRole);
            final long _tmpPairedAt;
            _tmpPairedAt = _cursor.getLong(_cursorIndexOfPairedAt);
            final long _tmpLastSyncedAt;
            _tmpLastSyncedAt = _cursor.getLong(_cursorIndexOfLastSyncedAt);
            final boolean _tmpIsAuthorized;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAuthorized);
            _tmpIsAuthorized = _tmp != 0;
            _result = new DeviceEntity(_tmpDeviceId,_tmpDeviceName,_tmpRole,_tmpPairedAt,_tmpLastSyncedAt,_tmpIsAuthorized);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
