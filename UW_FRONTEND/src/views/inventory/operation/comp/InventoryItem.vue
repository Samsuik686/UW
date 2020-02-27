<template>
    <div class="inventory-item" v-loading="isLoading">
        <div class="item-operation">
            <el-button
                    :disabled="disabledMaterialId !== ''"
                    @click="backInventoryBox"
                    type="primary">叉车回库
            </el-button>
            <el-table
                    :data="inventoryItem.list"
                    style="width:100%">
                <el-table-column
                        min-width="120"
                        prop="materialId"
                        label="料盘号">
                </el-table-column>
                <el-table-column
                        min-width="120"
                        prop="no"
                        label="料号">
                </el-table-column>
                <el-table-column
                        prop="specification"
                        label="规格">
                </el-table-column>
                <el-table-column
                        prop="supplier"
                        label="客户">
                </el-table-column>
                <el-table-column
                        prop="storeNum"
                        label="库存数量">
                </el-table-column>
                <el-table-column
                        label="盘点数量">
                    <template slot-scope="scope">
                        <quantity-input
                            :row="scope.row"
                            :activeMaterialId = "activeMaterialId"
                            :activeQuantity="activeQuantity"
                            :boxId="inventoryItem.boxId"
                            :taskId="inventoryItem.taskId"
                            :thisWindow = "thisWindow"
                            @setPosition="setPosition"
                        >
                        </quantity-input>
                    </template>
                </el-table-column>
                <el-table-column
                        label="操作">
                    <template slot-scope="scope">
                        <el-button
                                :type="printMaterialIdArr.includes(scope.row.materialId)?'warning':'primary'"
                                :disabled="disabledMaterialId === scope.row.materialId"
                                @click="setCheckQuantity(scope.row)">确定
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <div class="item-box" v-if="inventoryItem.boxId !== null">
            <material-box :col="col" :row="row" :id="inventoryItem.boxId"></material-box>
        </div>
    </div>
</template>

<script>
    import Bus from './../../../../utils/bus'
    import {mapGetters,mapActions} from 'vuex'
    import QuantityInput from "./subscomp/QuantityInput";
    import {axiosPost} from "../../../../utils/fetchData";
    import {backInventoryBoxUrl, printUrl} from "../../../../plugins/globalUrl";
    import {errHandler} from "../../../../utils/errorHandler";
    import MaterialBox from "./subscomp/MaterialBox";
    export default {
        name: "InventoryItem",
        components: {MaterialBox, QuantityInput},
        data(){
            return{
                isLoading:false,
                col:-1,
                row:-1
            }
        },
        computed:{
            ...mapGetters(['disabledMaterialId','user', 'configData','printMaterialIdArr'])
        },
        props:{
            inventoryItem:Object,
            activeMaterialId:String,
            activeQuantity:Number,
            thisWindow:String
        },
        watch:{
            activeMaterialId:function (val) {
                if(val !== ''){
                    for(let i =0;i<this.inventoryItem.list.length;i++){
                        let obj = this.inventoryItem.list[i];
                        if(obj.materialId === val){
                            this.col = obj.col;
                            this.row = obj.row;
                            return;
                        }
                    }
                }else{
                    this.col = -1;
                    this.row = -1;
                }
            }
        },
        methods:{
            ...mapActions(['setPrintMaterialIdArr']),
            backInventoryBox:function(){
                if(this.inventoryItem.taskId === null || this.inventoryItem.boxId === null){
                    this.$alertWarning('当前货位没有料盒');
                    return;
                }
                for(let i=0;i<this.inventoryItem.list.length;i++){
                    let item = this.inventoryItem.list[i];
                    if(item.actualNum === '' || item.actualNum === null){
                        this.$alertWarning('存在料盘未盘点');
                        return;
                    }
                }
                for(let i = 0;i<this.inventoryItem.list.length;i++){
                    let item = this.inventoryItem.list[i];
                    let isExit = false;
                    if(item.actualNum !== item.storeNum){
                        for(let j=0;j<this.printMaterialIdArr.length;j++){
                            if(item.materialId === this.printMaterialIdArr[j]){
                                isExit = true;
                                break;
                            }
                        }
                    }else{
                        isExit = true;
                    }
                    if(isExit === false){
                        this.$alertWarning('存在料盘未点击确定，打印新条码');
                        return;
                    }
                }
                if (!this.isPending) {
                    this.isPending = true;
                    this.isLoading = true;
                    let options = {
                        url: backInventoryBoxUrl,
                        data: {
                            taskId: this.inventoryItem.taskId,
                            boxId: this.inventoryItem.boxId,
                            windowId: this.inventoryItem.windowId,
                        }
                    };
                    axiosPost(options).then(response => {
                        if (response.data.result === 200) {
                            this.$alertSuccess('叉车回库成功');
                            this.cancelPrintArr();
                            Bus.$emit('refreshInventory',true);
                        } else {
                            errHandler(response.data);
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                        this.isLoading = false;
                    })
                }
            },
            setCheckQuantity:function(row){
                let actualNum = row.actualNum;
                if (actualNum === '' || actualNum === null) {
                    this.$alertWarning('盘点数量不能为空');
                    return;
                }
                if(!this.isNumber(actualNum)){
                    this.$alertWarning('盘点数量必须为非负整数');
                    return;
                }
                if (Number(row.storeNum) !== Number(actualNum)) {
                    if (this.configData.printerIP === "") {
                        this.$alertWarning("请在设置界面填写打印机IP");
                        return;
                    }
                }
                if (Number(row.storeNum) !== Number(actualNum)) {
                    if(Number(actualNum) === 0){
                        this.handlePrintMaterialArr(row.materialId);
                        return;
                    }
                    //this.printBarcode(row,actualNum);

                    this.handlePrintMaterialArr(row.materialId); //临时补丁
                }
            },
            printBarcode: function (row,actualNum) {
                let options = {
                    url:printUrl,
                    data: {
                        ip: this.configData.printerIP,
                        materialId: row.materialId,
                        quantity: actualNum
                    }
                };
                axiosPost(options).then(response => {
                    if (response.data.result === 200) {
                        this.$alertSuccess('打印成功');
                        this.handlePrintMaterialArr(row.materialId);
                    }else {
                        errHandler(response.data);
                    }
                }).catch(err => {
                    console.log(err);
                    this.$alertError('连接超时，请刷新重试');
                })
            },
            handlePrintMaterialArr:function(materialId){
                let arr = JSON.parse(JSON.stringify(this.printMaterialIdArr));
                arr.push(materialId);
                this.setPrintMaterialIdArr(arr);
            },
            isNumber: function (num) {
                let val = num;
                let reg = /^\+?(0|[1-9][0-9]*)$/;
                if (val !== "") {
                    return reg.test(val);
                }
            },
            cancelPrintArr:function () {
                let arr = [];
                for(let i=0;i<this.printMaterialIdArr.length;i++){
                    let materialId = this.printMaterialIdArr[i];
                    let isExit = false;
                    for(let i=0;i<this.inventoryItem.list.length;i++){
                        let item = this.inventoryItem.list[i];
                        if(materialId === item.materialId){
                            isExit = true;
                            break;
                        }
                    }
                    if(isExit === false){
                        arr.push(materialId);
                    }
                }
                this.setPrintMaterialIdArr(arr);
            },
            setPosition:function(col,row){
                this.col = col;
                this.row = row;
            }
        }
    }
</script>

<style scoped lang="scss">
    .inventory-item{
        .item-operation{
            width:calc(100% - 200px);
        }
        .item-box{
            width:200px;
            margin-left:20px;
        }
    }
</style>