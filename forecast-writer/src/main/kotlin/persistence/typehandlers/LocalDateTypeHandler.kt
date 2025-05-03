package cz.savic.weatherevaluator.forecastwriter.persistence.typehandlers

import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.CallableStatement
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.LocalDate

class LocalDateTypeHandler : BaseTypeHandler<LocalDate>() {
    
    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: LocalDate, jdbcType: JdbcType?) {
        ps.setDate(i, Date.valueOf(parameter))
    }
    
    override fun getNullableResult(rs: ResultSet, columnName: String): LocalDate? {
        val date = rs.getDate(columnName)
        return date?.toLocalDate()
    }
    
    override fun getNullableResult(rs: ResultSet, columnIndex: Int): LocalDate? {
        val date = rs.getDate(columnIndex)
        return date?.toLocalDate()
    }
    
    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): LocalDate? {
        val date = cs.getDate(columnIndex)
        return date?.toLocalDate()
    }
}