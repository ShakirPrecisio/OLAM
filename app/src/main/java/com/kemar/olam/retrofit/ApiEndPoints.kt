package com.kemar.olam.utility

import com.kemar.olam.bc_print_reprint_edit.model.request.BorderueLogDeleteReq
import com.kemar.olam.bc_print_reprint_edit.model.request.getBordereListByFilterReq
import com.kemar.olam.bordereau.model.request.AddBodereuReq
import com.kemar.olam.bordereau.model.request.AddBoereuLogListingReq
import com.kemar.olam.bordereau.model.request.BoderueDeleteLogReq
import com.kemar.olam.bordereau.model.request.ValidateBodereueNoReq
import com.kemar.olam.bordereau.model.responce.AddBodereuLogListingRes
import com.kemar.olam.bordereau.model.responce.AddBodereuRes
import com.kemar.olam.bordereau.model.responce.GetBodereuLogByIdRes
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.models.responce.GetForestDataRes
import com.kemar.olam.dashboard.models.responce.SupplierDatum
import com.kemar.olam.delivery_management.model.request.AddLogReq
import com.kemar.olam.delivery_management.model.request.DeliveryHistoryReq
import com.kemar.olam.delivery_management.model.request.RevokeLogsReq
import com.kemar.olam.delivery_management.model.response.AddHeaderForDeliveryRes
import com.kemar.olam.delivery_management.model.response.DeliveryMasterResponse
import com.kemar.olam.forestry_management.model.*
import com.kemar.olam.loading_wagons.model.request.AddLoadingBordereueHeaderReq
import com.kemar.olam.loading_wagons.model.request.GeneratePDFReq
import com.kemar.olam.loading_wagons.model.request.getBorderuaSerialNoReq
import com.kemar.olam.loading_wagons.model.responce.DeclrationBordereuListRes
import com.kemar.olam.loading_wagons.model.responce.GetLogDataByBarcodeRes
import com.kemar.olam.loading_wagons.model.responce.getBorderuaSerialNoRes
import com.kemar.olam.login.model.request.LoginReq
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.login.model.responce.LoginRes
import com.kemar.olam.offlineData.requestbody.BorderauHeaderLogRequest
import com.kemar.olam.offlineData.requestbody.LoadingHeaderLogRequest
import com.kemar.olam.offlineData.response.LogOfflineResponse
import com.kemar.olam.forestry_management.model.offlineForestryMasterDatum
import com.kemar.olam.origin_verification.model.RemoveLogFromStocksReq
import com.kemar.olam.origin_verification.model.request.OriginUserHistoryReq
import com.kemar.olam.physicalcount.requestbody.PhysicalCountRequest
import com.kemar.olam.physicalcount.response.StockListResponse
import com.kemar.olam.sales_and_inspection.inspection.model.request.SalesHeaderReq
import com.kemar.olam.sales_and_inspection.inspection.model.request.SalesInspectDataByVehicleNoReq
import com.lp.lpwms.ui.offline.response.OfflineResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import kotlin.collections.HashMap


interface ApiEndPoints {

    //for login
    @POST("InventoryApp/loginSubmit")
    fun login(@Body request: LoginReq): Call<LoginRes>

    //for token regeneration
    @POST("InventoryApp/ExtendLoginSessionToken")
    fun regenerateToken(@Body request: CommonRequest): Call<LoginRes>

    /*      Masters Data   */
    @POST("InventoryApp/GetForestDataByLocation")
    fun getForestDataByLocation(@Body request: CommonRequest): Call<List<SupplierDatum>>


    /*      Masters Data  for destination */
    @POST("InventoryApp/GetDestinationList")
    fun getGetDestinationList(@Body request: CommonRequest): Call<List<SupplierDatum>>

    /*      All Forest Masters Data   */
    @POST("InventoryApp/GetAllForestData")
    fun getAllForestData(@Body request: CommonRequest): Call<List<SupplierDatum>>



    /*      Masters Data   */
    /*@POST("InventoryApp/createDelivery")
    fun createDelivery(@Body request: CommonRequest): Call<Objects>*/

    @POST("InventoryApp/GetTransporterDataByMode")
    fun GetTransporterDataByMode(@Body request: CommonRequest):Call<List<SupplierDatum>>


     /*      Bordereau Creation   */
     @POST("InventoryApp/addBordereau")
     fun addBordereau(@Body request: AddBodereuReq): Call<AddBodereuRes>


   /*    Validate Logs    */
    @POST("InventoryApp/checkLogNumberDuplication")
    fun checkLogNumberDuplication(@Body request: CommonRequest): Call<AddBodereuRes>


    /*      Validate Bordereau No  */
    @POST("InventoryApp/checkBordereauRecorNumber")
    fun validateBordereauNo(@Body request: ValidateBodereueNoReq): Call<AddBodereuRes>


    /*      Validate log record No  */
    @POST("InventoryApp/checkLogRecorNumber")
    fun validateLogRecorNumber(@Body request: ValidateBodereueNoReq): Call<AddBodereuRes>

    /*        addBordereauLogs       */
    @POST("InventoryApp/addBordereauLogs")
    fun addBordereauLogs(@Body request: AddBoereuLogListingReq): Call<AddBodereuLogListingRes>

       //   BC Print , Reprint & Edit
     /*        get filter by borderue List      */
    @POST("InventoryApp/getBordereauListFilter")
    fun getBordereauListFilter(@Body request: getBordereListByFilterReq): Call<LogsUserHistoryRes>


    /*        userLogsHistory       */
    @POST("InventoryApp/getBordereauList")
    fun getBodereuLogsUserHistory(@Body request: CommonRequest): Call<LogsUserHistoryRes>


    /*        getBodereoAllLogs       */
    @POST("InventoryApp/getBordereauLogsByHeaderId?")
    fun getBodereuLogsByID(@Body request: CommonRequest): Call<GetBodereuLogByIdRes>


    /*        getMaxHeaderIdByBarcode       */
    @POST("InventoryApp/getMaxHeaderIdByBarcode")
    fun getBodereuLogsByID(@Body request: java.util.HashMap<String, Any>): Call<GetBodereuLogByIdRes>

    /*        delete bodereu log       */
    @POST("InventoryApp/deleteBordereauLog")
    fun deleteBordereauLog(@Body request: BoderueDeleteLogReq): Call<AddBodereuLogListingRes>


    /*        Bc print Reprinbt single & multiple delete bodereu log       */
    @POST("InventoryApp/deleteBordereauLogList")
    fun deleteBcPrintBordereauLog(@Body request: BorderueLogDeleteReq): Call<AddBodereuLogListingRes>


    @POST("InventoryApp/GetMasterDataByForestID")
    fun getMasterDataByForestID(@Body request: CommonRequest): Call<GetForestDataRes>

    @POST("InventoryApp/getVehicleDataByChargementId")
    fun getMasterDataForVehicle(@Body request: CommonRequest): Call<List<SupplierDatum?>?>


    //for origin Verification vehicle data master
    @POST("InventoryApp/GetDropDownDataForOrigin")
    fun GetDropDownDataForOriginVehicle(@Body request: CommonRequest): Call<GetForestDataRes>


    @POST("InventoryApp/GetLogMasterByForestID")
    fun getLogsMaster(@Body request: CommonRequest): Call<GetForestDataRes>


   /* @GET("InventoryApp/GetTransporterDataByMode?")
    fun getTransporterDataByMode(@Query("transportModeID") transportModeID: String): Call<List<SupplierDatum>>
*/

    /*         Loading Wagons APIs         */

    /*       search By Log No    */
    @POST("InventoryApp/getLogDataByLogNo")
    fun getLogDataByLLoogNo(@Body request: CommonRequest): Call<GetLogDataByBarcodeRes>

    @POST("InventoryApp/getLogDataByLogNoForLoadingWagons")
    fun getLogDataByLogNoForLoadingWagons(@Body request: CommonRequest): Call<GetLogDataByBarcodeRes>

    @POST("InventoryApp/getLogDataByLogNoForOriginVerification")
    fun getLogDataByLogNoForOriginVerification(@Body request: CommonRequest): Call<GetLogDataByBarcodeRes>




 //for deleting bordreu from Declaration
 @POST("InventoryApp/deleteLoadedBordereau")
 fun deleteLoadedBordereau(@Body request: CommonRequest): Call<GetLogDataByBarcodeRes>

    /*        Loading Wagons History       */
    @POST("InventoryApp/getBordereauListForLoading")
    fun getLoadingWagonsUserHistory(@Body request: CommonRequest): Call<LogsUserHistoryRes>

    /*        Loading Wagons Reprint       */
    @POST("InventoryApp/reprintDeclaredInoive")
    fun reprintDeclaredInoive(@Body request: CommonRequest): Call<AddBodereuLogListingRes>


    /*        add Bordereau for Loading wagons      */
    @POST("InventoryApp/addBordereauforLoading")
    fun addBordereauforLoadingWagons(@Body request: AddLoadingBordereueHeaderReq): Call<AddBodereuRes>


    /*        add Bordereau for Loading wagons      */
    @POST("InventoryApp/deleteLogFromDeclare")
    fun deleteLogFromDeclare(@Body request: CommonRequest): Call<AddBodereuRes>

    /*       fetch barcode details for Loading wagons      */
    @POST("InventoryApp/getLogDataByBarCode?")
    fun getLogDataByBarCode(@Body request: CommonRequest): Call<GetLogDataByBarcodeRes>

    /*       fetch barcode details for Loading wagons      */
    @POST("InventoryApp/getLogDataByBarCodeInOrigin?")
    fun getLogDataByBarCodeInOrigin(@Body request: CommonRequest): Call<GetLogDataByBarcodeRes>


    /*        addBordereauLogsForLoadingWagons       */
    @POST("InventoryApp/addBordereauLogsForLoading")
    fun addBordereauLogsForLoading(@Body request: AddBoereuLogListingReq): Call<AddBodereuLogListingRes>

    /*        get Borderue Serial Number       */
    @POST("InventoryApp/getBodereauCount")
    fun getBordereuSerialNumber(@Body request: getBorderuaSerialNoReq): Call<getBorderuaSerialNoRes>

    /*        get Laoding Wagons Declration List       */
    @POST("InventoryApp/getBordereauListDeclaration")
    fun getBordereauListDeclaration(@Body request: CommonRequest): Call<DeclrationBordereuListRes>


    /*        Generate loading wagons pdf       */
    @POST("InventoryApp/getPDFFileForLoaded")
    fun getPDFFileForLoaded(@Body request: GeneratePDFReq): Call<AddBodereuLogListingRes>


    /*        Generate loading wagons pdf       */
    @POST("InventoryApp/declareLoadedBordereau")
    fun declareLoadedBordereau(@Body request: GeneratePDFReq): Call<AddBodereuLogListingRes>


    @POST("InventoryApp/getOriginFilteredList")
    fun getOriginFilteredList(@Body request: OriginUserHistoryReq): Call<LogsUserHistoryRes>

    @POST("InventoryApp/saveLogInOriginVerify")
    fun saveLogInOriginVerify(@Body request: AddBoereuLogListingReq): Call<AddBodereuLogListingRes>


    @POST("InventoryApp/deleteLogListFromOrigin")
    fun removeLogListFromOrigin(@Body request: RemoveLogFromStocksReq): Call<AddBodereuLogListingRes>

    @POST("InventoryApp/getDeleteLogFromOrigin")
    fun getDeleteLogFromOrigin(@Body request: CommonRequest): Call<GetBodereuLogByIdRes>

    @POST("InventoryApp/saveDeleteLogFromOrigin")
    fun saveDeleteLogFromOrigin(@Body request: RemoveLogFromStocksReq): Call<GetBodereuLogByIdRes>


     /*     Sales & Inspection    */
     @POST("InventoryApp/GetMasterDataSalesInspection?")
     fun getMasterDataSalesInspection(@Body request: CommonRequest): Call<GetForestDataRes>

   /* @GET("InventoryApp/GetWagonDataForInspection?")
    fun getWagonDataForInspection(@Query("userLocationID") userLocationID: String): Call<List<SupplierDatum>>*/


    @POST("InventoryApp/GetSalesInspectDataByVehicleNo")
    fun getSalesInspectDataByVehicleNo(@Body request: SalesInspectDataByVehicleNoReq): Call<AddBodereuRes>


    /*        Sales & Inspection History       */
    @POST("InventoryApp/getBordereauForInspection")
    fun getBordereauHistoryForInspection(@Body request: CommonRequest): Call<LogsUserHistoryRes>


    /*      Sales Inspection Bordereau Creation   */
    @POST("InventoryApp/addInspectionHeader")
    fun addSalesInspectionBordereau(@Body request: SalesHeaderReq): Call<AddBodereuRes>

    /*        getSalesBodereoAllLogs       */
    @POST("InventoryApp/getInspectionLogsByHeaderId")
    fun getSalesBodereuLogsByID(@Body request: CommonRequest): Call<GetBodereuLogByIdRes>

    /*      Sales No Inspection  */
    @POST("InventoryApp/changeToNoInspectionById")
    fun noInspection(@Body request: BoderueDeleteLogReq): Call<AddBodereuLogListingRes>

    /*        submit for approval       */
    @POST("InventoryApp/submitInspectionLogs")
    fun addSalesBordereauLogs(@Body request: AddBoereuLogListingReq): Call<AddBodereuLogListingRes>

    /*        confirm for approval       */
    @POST("InventoryApp/approveInspection")
    fun confirmForBilling(@Body request: AddBoereuLogListingReq): Call<AddBodereuLogListingRes>

    /*        confirm for approval       */
    @POST("InventoryApp/pdfPreviewSalesInvoice")
    fun pdfPreviewSalesInvoice(@Body request: AddBoereuLogListingReq): Call<AddBodereuLogListingRes>


/*
    @POST("InventoryApp/approveInspection")
    fun confirmForBilling(@Body request: JSONObject): Call<AddBodereuLogListinfRes>
*/

    @POST("InventoryApp/deleteLogById")
    fun deleteInspectionLog(@Body request: BoderueDeleteLogReq): Call<AddBodereuLogListingRes>

    @POST("InventoryApp/reprintSalesInoive")
    fun reprintSalesInoive(@Body request: BoderueDeleteLogReq): Call<AddBodereuLogListingRes>


    //Signimage upload for user
    @Multipart
    @POST("InventoryApp/digitalSignUpload")
    fun uploadSign( @Part("files") imgName: RequestBody?,
                    @Part photo: MultipartBody.Part?): Call<AddBodereuLogListingRes>


    @POST("InventoryApp/getDeliveryHistory")
    fun getDeliveryHistory(/*@Body request: DeliveryHistoryReq*/): Call<com.kemar.olam.delivery_management.model.response.LogsUserHistoryRes>

    @POST("InventoryApp/confirmDelivery")
    fun confirmDelivery(@Body request: AddBoereuLogListingReq): Call<AddBodereuLogListingRes>

   /* @GET("InventoryApp/getDeliveryLogsByHeaderId?")
    fun getDeliveryBodereuLogsByID(@Query("bordereauHeaderId") headerId: String
    ): Call<GetBodereuLogByIdRes>*/

    @POST("InventoryApp/getAllLogByInspectionId")
    fun getDeliveryBodereuLogsByID(@Body request: CommonRequest): Call<GetBodereuLogByIdRes>


    //API For tranporteer & Vehicler Confirm Delivery
    @POST("InventoryApp/GetMasterDataForDelivery")
    fun getMasterDataForDelivery(@Body request: CommonRequest): Call<GetForestDataRes>

    //API For Deliver Header tranporteer & Vehicle
    @POST("InventoryApp/GetMasterDataForDelivery")
    fun getMasterDataForDeliveryHeader(@Body request: CommonRequest): Call<GetForestDataRes>


    //this is for delivered Bordereu History
    @POST("InventoryApp/getDeliveredLogList")
    fun getDeliveredLogList(@Body request: DeliveryHistoryReq): Call<com.kemar.olam.delivery_management.model.response.LogsUserHistoryRes>

    //Revoke Delivered Logs
    @POST("InventoryApp/returnLogs")
    fun revokeDeliveredLogsList(@Body request: RevokeLogsReq): Call<AddBodereuLogListingRes>


    /*     For Ground Sales    */
 //earlier it was get method with no request param
    @POST("InventoryApp/getGroundLogsForInspection")
    fun getGroundLogsForInspection(@Body request: CommonRequest): Call<GetBodereuLogByIdRes>

    /*       get Log details by barcode for ground sales      */
    @POST("InventoryApp/getGroundLogDataByBarCode")
    fun getGroundLogDataByBarCode(@Body request: CommonRequest): Call<GetBodereuLogByIdRes>

    //for adding ground header
    @POST("InventoryApp/addInspectionHeaderForGround")
    fun addInspectionHeaderForGround(@Body request: SalesHeaderReq): Call<AddBodereuRes>


    //for ground user history & For approval listing
    @POST("InventoryApp/getBordereauForGround")
    fun getBordereauHistoryForGround(@Body request: CommonRequest): Call<LogsUserHistoryRes>


    /*        getGroundInspctionIdsBodereoAllLogs       */
    @POST("InventoryApp/getGroundLogByInspectionId?")
    fun getGroundBodereuLogsByID(@Body request: CommonRequest ): Call<GetBodereuLogByIdRes>


    /*        submit for ground approval       */
    @POST("InventoryApp/submitGroundLogs")
    fun sendForGroundApproval(@Body request: AddBoereuLogListingReq): Call<AddBodereuLogListingRes>


    /*        submit for ground approval       */
    @POST("InventoryApp/approveGroundInspection")
    fun sendForGroundConfirm(@Body request: AddBoereuLogListingReq): Call<AddBodereuLogListingRes>


 /*        get forest master for deliver       */
 @POST("InventoryApp/GetForestForDelivery")
 fun getForestDataByLocationForDelivery(@Body request: CommonRequest): Call<List<SupplierDatum>>


@POST("InventoryApp/GetDeliveryMasterData")
fun getDeliveryMaster(): Call<DeliveryMasterResponse>


 @POST("InventoryApp/GetMasterForDeliveryHeader?")
 fun getForestMasterForDelivery(@Body request: CommonRequest): Call<GetForestDataRes>

 /*      Bordereau Creation for delivery   */
 @POST("InventoryApp/createDelivery")
 fun addBordereauForDelivery(@Body request: AddBodereuReq): Call<AddHeaderForDeliveryRes>


 /*       fetch barcode details by barcode delivery     */
 @POST("InventoryApp/getLogDataByBarCodeDelivery")
 fun getLogDataByBarCodeDeliver(@Body request: CommonRequest): Call<GetLogDataByBarcodeRes>


 /*       fetch barcode details by barcode delivery     */
 @POST("InventoryApp/getLogDataByLogNoDelivery")
 fun getLogDataByLogNoDelivery(@Body request: CommonRequest): Call<GetLogDataByBarcodeRes>


 //fetch deliervy log by delivery id
 @POST("InventoryApp/getLogByDeliveryId")
 fun getLogByDeliveryId(@Body request: CommonRequest): Call<GetBodereuLogByIdRes>


 @POST("InventoryApp/saveDelivery")
 fun saveDeliveryLog(@Body request: AddLogReq): Call<AddHeaderForDeliveryRes>

 @POST("InventoryApp/addLogToDelivery")
 fun addLogToDelivery(@Body request: AddLogReq): Call<AddHeaderForDeliveryRes>


 @POST("InventoryApp/deleteDelivery")
 fun removeeDeliveryLog(@Body request: AddLogReq): Call<AddHeaderForDeliveryRes>

 @POST("InventoryApp/removeLogFromDelivery")
 fun removeLogFromDelivery(@Body request: AddLogReq): Call<AddHeaderForDeliveryRes>

 //for printing delivery invoice
 @POST("InventoryApp/reprintDeliveryInoive")
 fun reprintDeliveryInoive(@Body request: CommonRequest): Call<AddBodereuLogListingRes>

 @POST("InventoryApp/GetOfflineMasterData")
 fun getOfflineMasterData(@Body request: CommonRequest): Call<OfflineResponse>

 @POST("InventoryApp/getAllMasterDropDownDtls")
 fun getOfflineForestryMasterData(@Body request: CommonRequest): Call<offlineForestryMasterDatum>

 @POST("InventoryApp/getAllMasterDataDropDownDtls")
 fun getOfflineForestryMasterDataCategory2(@Body request: CommonRequest): Call<offlineForestryMasterDatum>

 @POST("InventoryApp/getAllLogDataForLoadingWagons")
 fun getAllLogDataForLoadingWagons (@Body request: CommonRequest): Call<LogOfflineResponse>

 @POST("InventoryApp/syncBordereauModule")
 fun syncBordereauModule (@Body request: BorderauHeaderLogRequest): Call<AddBodereuRes>

 @POST("InventoryApp/syncLoadingWagonModule")
 fun syncLoadingWagonModule (@Body request: LoadingHeaderLogRequest): Call<AddBodereuRes>

 @POST("InventoryApp/saveStockList")
 fun physicalLogScan (@Body physicalCountRequest: PhysicalCountRequest): Call<AddBodereuRes>

 @POST("InventoryApp/getStockLists ")
 fun getStockLists  (@Body hashMap: HashMap<String,Any>): Call<StockListResponse>

 @POST("InventoryApp/getStockListInfo  ")
 fun getStockListInfo   (@Body hashMap: HashMap<String,Any>): Call<StockListResponse>

/*//for printing delivery invoice
@POST("InventoryApp/previewDelivery")
fun reprintDeliveryInoive(@Body request: CommonRequest): Call<AddBodereuLogListingRes>*/

    /*        generateBarcodes       */
//    @POST("InventoryApp/addBarcodeGeneration")
//    fun generateBarcodes(@Body request: BarcodeGenerationRequest): Call<AddBodereuLogListingRes>

    /*        generateBarcodes       */
    @POST("InventoryApp/generateBarcode")
    fun generateBarcodes(@Body request: BarcodeGenerationRequest): Call<GetGeneratedBarcodeResponse>

    /*        getBarcodeList       */
    @POST("InventoryApp/getBarCodeDetails")
    fun getBarcodeList(@Body request: HashMap<String, Int>): Call<GetBarcodeListResponse>


    /*        addTreeDetails       */
    @POST("InventoryApp/addTreeDetail")
    fun addTreeDetails(@Body request: AddTreeDetailsRequest): Call<AddBodereuLogListingRes>

    /*        getTreeDetailList       */
    @POST("InventoryApp/getTreeDetailList ")
    fun getTreeDetailList(@Body request: HashMap<String, Int>): Call<TreeInventoryListResponse>

    /*        deleteTreeDetail       */
    @POST("InventoryApp/deleteTreeDetail")
    fun deleteTreeDetail(@Body request: HashMap<String, Long>): Call<TreeInventoryListResponse>

    /*        addPistageDetails       */
    @POST("InventoryApp/addPistage")
    fun addPistageDetails(@Body request: AddPistageDetailsRequest): Call<AddBodereuLogListingRes>

    /*        addTreeFellingScanDetails       */
    @POST("InventoryApp/addTreeFellingScan")
    fun addTreeFellingScanDetails(@Body request: AddTreeFellingScanDetailsRequest): Call<AddBodereuLogListingRes>

    /*        Tree Pulling Scan       */
    @POST("InventoryApp/addTreePullingScan")
    fun addTreePullingScanDetails(@Body request: AddTreePullingScanDetailsRequest): Call<AddBodereuLogListingRes>

    /*        Log Cutting Scan       */
    @POST("InventoryApp/addLogCuttingScan")
    fun addLogCuttingScanDetails(@Body request: AddLogCuttingScanDetailsRequest): Call<AddBodereuLogListingRes>


}