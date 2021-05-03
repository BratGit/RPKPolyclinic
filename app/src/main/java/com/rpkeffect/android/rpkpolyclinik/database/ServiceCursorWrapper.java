package com.rpkeffect.android.rpkpolyclinik.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.rpkeffect.android.rpkpolyclinik.classes.OrderedService;

public class ServiceCursorWrapper extends CursorWrapper {
    public ServiceCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public OrderedService getService(){
        int id = getInt(getColumnIndex(UserDBSchema.OrderedServicesTable.Cols.ID));
        int serviceId = getInt(getColumnIndex(UserDBSchema.OrderedServicesTable.Cols.SERVICE_ID));
        String userId = getString(getColumnIndex(UserDBSchema.OrderedServicesTable.Cols.USER_ID));

        OrderedService orderedService = new OrderedService();
        orderedService.setId(id);
        orderedService.setServiceId(serviceId);
        orderedService.setUserId(userId);

        return orderedService;
    }
}
