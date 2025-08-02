package com.vibecoder.app.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import com.vibecoder.app.models.Customer
import com.vibecoder.app.models.ExportData
import com.vibecoder.app.models.FollowUp
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    
    suspend fun exportToCSV(context: Context, exportData: ExportData): Uri? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "vibe_coder_export_$timestamp.csv"
            
            val file = File(context.getExternalFilesDir(null), fileName)
            val writer = CSVWriter(FileWriter(file))
            
            // Write headers
            writer.writeNext(arrayOf(
                "Type", "ID", "Name", "Phone", "Amount", "Promise Date", 
                "Notes", "Name Editable", "Phone Editable", "Amount Editable",
                "Created At", "Updated At", "Customer ID", "Timestamp", "Next Promise Date"
            ))
            
            // Write customers
            exportData.customers.forEach { customer ->
                writer.writeNext(arrayOf(
                    "Customer",
                    customer.id,
                    customer.name,
                    customer.phoneNumber,
                    customer.amount.toString(),
                    customer.promiseDate.toString(),
                    customer.notes,
                    customer.nameEditable.toString(),
                    customer.phoneEditable.toString(),
                    customer.amountEditable.toString(),
                    customer.createdAt.toString(),
                    customer.updatedAt.toString(),
                    "", "", "" // Empty for follow-up fields
                ))
            }
            
            // Write follow-ups
            exportData.followUps.forEach { followUp ->
                writer.writeNext(arrayOf(
                    "FollowUp",
                    followUp.id,
                    "", "", "", "", // Empty for customer fields
                    followUp.notes,
                    "", "", "", "", "", // Empty for customer fields
                    followUp.customerId,
                    followUp.timestamp.toString(),
                    followUp.nextPromiseDate?.toString() ?: ""
                ))
            }
            
            writer.close()
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun exportToExcel(context: Context, exportData: ExportData): Uri? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "vibe_coder_export_$timestamp.xlsx"
            
            val file = File(context.getExternalFilesDir(null), fileName)
            val workbook = XSSFWorkbook()
            
            // Create customers sheet
            val customerSheet = workbook.createSheet("Customers")
            val customerHeaderRow = customerSheet.createRow(0)
            val customerHeaders = arrayOf(
                "ID", "Name", "Phone", "Amount", "Promise Date", "Notes",
                "Name Editable", "Phone Editable", "Amount Editable",
                "Created At", "Updated At"
            )
            customerHeaders.forEachIndexed { index, header ->
                customerHeaderRow.createCell(index).setCellValue(header)
            }
            
            exportData.customers.forEachIndexed { index, customer ->
                val row = customerSheet.createRow(index + 1)
                row.createCell(0).setCellValue(customer.id)
                row.createCell(1).setCellValue(customer.name)
                row.createCell(2).setCellValue(customer.phoneNumber)
                row.createCell(3).setCellValue(customer.amount)
                row.createCell(4).setCellValue(customer.promiseDate.toString())
                row.createCell(5).setCellValue(customer.notes)
                row.createCell(6).setCellValue(customer.nameEditable)
                row.createCell(7).setCellValue(customer.phoneEditable)
                row.createCell(8).setCellValue(customer.amountEditable)
                row.createCell(9).setCellValue(customer.createdAt.toDouble())
                row.createCell(10).setCellValue(customer.updatedAt.toDouble())
            }
            
            // Create follow-ups sheet
            val followUpSheet = workbook.createSheet("FollowUps")
            val followUpHeaderRow = followUpSheet.createRow(0)
            val followUpHeaders = arrayOf(
                "ID", "Customer ID", "Notes", "Timestamp", "Next Promise Date"
            )
            followUpHeaders.forEachIndexed { index, header ->
                followUpHeaderRow.createCell(index).setCellValue(header)
            }
            
            exportData.followUps.forEachIndexed { index, followUp ->
                val row = followUpSheet.createRow(index + 1)
                row.createCell(0).setCellValue(followUp.id)
                row.createCell(1).setCellValue(followUp.customerId)
                row.createCell(2).setCellValue(followUp.notes)
                row.createCell(3).setCellValue(followUp.timestamp.toDouble())
                row.createCell(4).setCellValue(followUp.nextPromiseDate?.toString() ?: "")
            }
            
            val fileOutputStream = FileOutputStream(file)
            workbook.write(fileOutputStream)
            fileOutputStream.close()
            workbook.close()
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun importFromCSV(context: Context, uri: Uri): ExportData? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = CSVReader(InputStreamReader(inputStream))
            
            val customers = mutableListOf<Customer>()
            val followUps = mutableListOf<FollowUp>()
            
            val lines = reader.readAll()
            lines.drop(1).forEach { line -> // Skip header
                if (line.isNotEmpty()) {
                    when (line[0]) {
                        "Customer" -> {
                            if (line.size >= 12) {
                                try {
                                    val customer = Customer(
                                        id = line[1],
                                        name = line[2],
                                        phoneNumber = line[3],
                                        amount = line[4].toDouble(),
                                        promiseDate = LocalDate.parse(line[5]),
                                        notes = line[6],
                                        nameEditable = line[7].toBoolean(),
                                        phoneEditable = line[8].toBoolean(),
                                        amountEditable = line[9].toBoolean(),
                                        createdAt = line[10].toLong(),
                                        updatedAt = line[11].toLong()
                                    )
                                    customers.add(customer)
                                } catch (e: Exception) {
                                    // Skip invalid customer entries
                                }
                            }
                        }
                        "FollowUp" -> {
                            if (line.size >= 15) {
                                try {
                                    val followUp = FollowUp(
                                        id = line[1],
                                        customerId = line[12],
                                        notes = line[6],
                                        timestamp = line[13].toLong(),
                                        nextPromiseDate = if (line[14].isNotEmpty()) LocalDate.parse(line[14]) else null
                                    )
                                    followUps.add(followUp)
                                } catch (e: Exception) {
                                    // Skip invalid follow-up entries
                                }
                            }
                        }
                    }
                }
            }
            
            reader.close()
            ExportData(customers, followUps)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun importFromExcel(context: Context, uri: Uri): ExportData? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val workbook = WorkbookFactory.create(inputStream)
            
            val customers = mutableListOf<Customer>()
            val followUps = mutableListOf<FollowUp>()
            
            // Read customers sheet
            val customerSheet = workbook.getSheet("Customers")
            if (customerSheet != null) {
                for (i in 1..customerSheet.lastRowNum) {
                    val row = customerSheet.getRow(i)
                    if (row != null) {
                        try {
                            val customer = Customer(
                                id = row.getCell(0)?.stringCellValue ?: continue,
                                name = row.getCell(1)?.stringCellValue ?: "",
                                phoneNumber = row.getCell(2)?.stringCellValue ?: "",
                                amount = row.getCell(3)?.numericCellValue ?: 0.0,
                                promiseDate = LocalDate.parse(row.getCell(4)?.stringCellValue ?: continue),
                                notes = row.getCell(5)?.stringCellValue ?: "",
                                nameEditable = row.getCell(6)?.booleanCellValue ?: true,
                                phoneEditable = row.getCell(7)?.booleanCellValue ?: true,
                                amountEditable = row.getCell(8)?.booleanCellValue ?: true,
                                createdAt = row.getCell(9)?.numericCellValue?.toLong() ?: System.currentTimeMillis(),
                                updatedAt = row.getCell(10)?.numericCellValue?.toLong() ?: System.currentTimeMillis()
                            )
                            customers.add(customer)
                        } catch (e: Exception) {
                            // Skip invalid customer entries
                        }
                    }
                }
            }
            
            // Read follow-ups sheet
            val followUpSheet = workbook.getSheet("FollowUps")
            if (followUpSheet != null) {
                for (i in 1..followUpSheet.lastRowNum) {
                    val row = followUpSheet.getRow(i)
                    if (row != null) {
                        try {
                            val followUp = FollowUp(
                                id = row.getCell(0)?.stringCellValue ?: continue,
                                customerId = row.getCell(1)?.stringCellValue ?: continue,
                                notes = row.getCell(2)?.stringCellValue ?: "",
                                timestamp = row.getCell(3)?.numericCellValue?.toLong() ?: System.currentTimeMillis(),
                                nextPromiseDate = row.getCell(4)?.stringCellValue?.let { 
                                    if (it.isNotEmpty()) LocalDate.parse(it) else null 
                                }
                            )
                            followUps.add(followUp)
                        } catch (e: Exception) {
                            // Skip invalid follow-up entries
                        }
                    }
                }
            }
            
            workbook.close()
            inputStream?.close()
            
            ExportData(customers, followUps)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}