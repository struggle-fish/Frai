package com.example

import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.runBlocking

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testFetchModelsOnline() {
    runBlocking {
      val baseUrl = "https://api-a.frai.live"
      try {
        println("DIAGNOSTIC: Testing Online URL: $baseUrl")
        val service = com.example.data.api.ApiService.create(baseUrl)
        
        // Fetch categories first
        val catResponse = service.getAppModelList(System.currentTimeMillis())
        println("DIAGNOSTIC: getAppModelList code: ${catResponse.code}, msg: ${catResponse.msg}")
        val parents = catResponse.data ?: emptyList()
        println("DIAGNOSTIC: Parents count: ${parents.size}")
        
        parents.forEach { parent ->
          println("DIAGNOSTIC: Category Parent ID: ${parent.id}, Name: ${parent.name}, Type: ${parent.modelType}")
          val detailsResponse = service.getAppModelDetailsList(id = parent.id)
          val childList = detailsResponse.data?.childList ?: emptyList()
          println("  -> Details Response code: ${detailsResponse.code}, Child list size: ${childList.size}")
          childList.forEach { child ->
            println("     ==> Child ID: ${child.id}, Model: ${child.model}, Name: ${child.name}, Points: ${child.points}")
          }
        }
      } catch (e: Exception) {
        println("DIAGNOSTIC: Online URL failed!")
        e.printStackTrace()
      }
    }
  }

  @Test
  fun testFetchModelsTest() {
    runBlocking {
      val baseUrl = "https://api.tacpay.cn"
      try {
        println("DIAGNOSTIC: Testing Test URL: $baseUrl")
        val service = com.example.data.api.ApiService.create(baseUrl)

        // Fetch categories first
        val catResponse = service.getAppModelList(System.currentTimeMillis())
        println("DIAGNOSTIC: getAppModelList code: ${catResponse.code}, msg: ${catResponse.msg}")
        val parents = catResponse.data ?: emptyList()
        println("DIAGNOSTIC: Parents count: ${parents.size}")

        parents.forEach { parent ->
          println("DIAGNOSTIC: Category Parent ID: ${parent.id}, Name: ${parent.name}, Type: ${parent.modelType}")
          val detailsResponse = service.getAppModelDetailsList(id = parent.id)
          val childList = detailsResponse.data?.childList ?: emptyList()
          println("  -> Details Response code: ${detailsResponse.code}, Child list size: ${childList.size}")
          childList.forEach { child ->
            println("     ==> Child ID: ${child.id}, Model: ${child.model}, Name: ${child.name}, Points: ${child.points}")
          }
        }
      } catch (e: Exception) {
        println("DIAGNOSTIC: Test URL failed!")
        e.printStackTrace()
      }
    }
  }
}


