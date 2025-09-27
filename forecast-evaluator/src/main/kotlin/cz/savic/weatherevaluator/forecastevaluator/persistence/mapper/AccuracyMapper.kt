package cz.savic.weatherevaluator.forecastevaluator.persistence.mapper

import cz.savic.weatherevaluator.forecastevaluator.persistence.entity.DailyAccuracyEntity
import cz.savic.weatherevaluator.forecastevaluator.persistence.entity.HourlyAccuracyEntity
import org.apache.ibatis.annotations.Mapper

@Mapper
interface AccuracyMapper {

    fun insertHourlyAccuracy(entity: HourlyAccuracyEntity)

    fun insertHourlyAccuracyBatch(entities: List<HourlyAccuracyEntity>)

    fun insertDailyAccuracy(entity: DailyAccuracyEntity)

    fun insertDailyAccuracyBatch(entities: List<DailyAccuracyEntity>)

    fun countHourlyAccuracyRecords(): Long

    fun countDailyAccuracyRecords(): Long
}