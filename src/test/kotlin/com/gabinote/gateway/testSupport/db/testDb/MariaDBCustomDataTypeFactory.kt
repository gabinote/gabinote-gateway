package com.gabinote.api.testSupport.testConfig.db

import org.dbunit.dataset.datatype.DataType
import org.dbunit.ext.mysql.MySqlDataTypeFactory

class MariaDBCustomDataTypeFactory : MySqlDataTypeFactory() {
    override fun getValidDbProducts(): List<String> {
        return listOf("MariaDB", "MySQL")
    }

    override fun createDataType(sqlType: Int, sqlTypeName: String?): DataType {
        if (sqlTypeName == "UUID") {
            return DataType.VARCHAR
        }

        return super.createDataType(sqlType, sqlTypeName)
    }
}