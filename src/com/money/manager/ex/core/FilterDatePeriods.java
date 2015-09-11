/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.core;

import com.money.manager.ex.R;

/**
 * Filter date periods. Not user yet. Needs to be completed.
 */
public enum FilterDatePeriods {
    ALLTIME(R.string.all_time),
;

    public static FilterDatePeriods get(int code) {
        for (FilterDatePeriods value : FilterDatePeriods.values()) {
            int currentCode = value.getCode();
            if (currentCode == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("no transaction status found for " + code);
//        return null;
    }

    FilterDatePeriods(int code) {
        this.code = code;
    }

    private final int code;

    public int getCode() {
        return this.code;
    }

//    public TransactionTypes from(String name) {
//
//    }

    public boolean contains(String name) {
        boolean result = false;

        for (FilterDatePeriods type : FilterDatePeriods.values()) {
            if (type.toString().equalsIgnoreCase(name)) {
                result = true;
                break;
            }
        }

        return result;
    }
}