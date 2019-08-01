<template>
    <div class="sample-item" v-loading="isLoading">
        <div class="item-operation">
            <el-form :inline="true" size="medium">
                <el-form-item label="料盒总盘数">
                    <el-input v-model="sampleItem.totalNum" disabled></el-input>
                </el-form-item>
                <el-form-item label="已扫盘数">
                    <el-input v-model="sampleItem.scanNum" disabled></el-input>
                </el-form-item>
                <el-form-item label="已出库盘数">
                    <el-input v-model="sampleItem.outNum" disabled></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button
                            @click="backInventoryBox"
                            type="primary">叉车回库
                    </el-button>
                </el-form-item>
            </el-form>
            <el-table
                    :data="sampleItem.list"
                    style="width:100%">
                <el-table-column
                        min-width="100"
                        prop="materialId"
                        label="料盘号">
                    <template slot-scope="scope">
                        <high-light
                            :row="scope.row"
                            :active-material-id="activeMaterialId"
                        ></high-light>
                    </template>
                </el-table-column>
                <el-table-column
                        min-width="100"
                        prop="no"
                        label="料号">
                </el-table-column>
                <el-table-column
                        prop="specification"
                        label="规格">
                </el-table-column>
                <el-table-column
                        prop="supplier"
                        label="供应商">
                </el-table-column>
                <el-table-column
                        prop="storeNum"
                        label="数量">
                </el-table-column>
                <el-table-column
                        min-width="200"
                        label="操作">
                    <template slot-scope="scope">
                        <el-button
                                size="small"
                                :type="scope.row.isOuted === true?'info':'primary'"
                                :disabled="scope.row.isOuted === true"
                                @click="outSingular(scope.row.materialId)">异常出库
                        </el-button>
                        <el-button
                                size="small"
                                :type="scope.row.isOuted === true?'info':'primary'"
                                :disabled="scope.row.isOuted === true"
                                @click="outRegular(scope.row.materialId)">抽检出库
                        </el-button>
                        <el-button
                                size="small"
                                :type="scope.row.isOuted === true?'info':'primary'"
                                :disabled="scope.row.isOuted === true"
                                @click="outLost(scope.row.materialId)">料盘丢失
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <div class="item-box" v-if="sampleItem.boxId !== null">
            <material-box
                :id="sampleItem.boxId"
                :col="col"
                :row="row"
                :list="sampleItem.list"
            ></material-box>
        </div>
    </div>
</template>

<script>
    import {
        backBoxSampleTaskUrl, outLostSampleTaskUrl,
        outRegularSampleTaskUrl,
        outSingularSampleTaskUrl
    } from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";
    import HighLight from "./subscomp/HighLight";
    import MaterialBox from "./subscomp/MaterialBox"

    export default {
        name: "SampleItem",
        components: {MaterialBox, HighLight},
        props:{
            sampleItem:Object,
            activeName:String,
            activeMaterialId:String,
            isScan:Boolean
        },
        data(){
            return{
                isLoading:false,
                col:-1,
                row:-1
            }
        },
        watch:{
            activeMaterialId:function(val){
                if(val !== ''){
                    if(this.sampleItem.list === null){
                        return;
                    }
                    for(let i =0;i<this.sampleItem.list.length;i++){
                        let obj = this.sampleItem.list[i];
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
            },
            isScan:function(val){
                if(val === true && this.sampleItem.boxId === Number(this.activeName)){
                    for(let i =0;i<this.sampleItem.list.length;i++){
                        let materialId = this.sampleItem.list[i].materialId;
                        if(this.activeMaterialId === materialId){
                            this.textToSpeak('已扫'+this.sampleItem.scanNum+'盘还剩'+(this.sampleItem.totalNum - this.sampleItem.scanNum)+'盘');
                            this.$emit("update:isScan",false);
                        }
                    }
                }
            }
        },
        methods:{
            backInventoryBox:function(){
                if (this.sampleItem.groupId === '') {
                    return;
                }
                if (!this.isPending) {
                    this.isPending = true;
                    this.isLoading = true;
                    let options = {
                        url: backBoxSampleTaskUrl,
                        data: {
                            groupId: this.sampleItem.groupId
                        }
                    };
                    axiosPost(options).then(response => {
                        if (response.data.result === 200) {
                            this.$alertSuccess('叉车回库成功');
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
            outRegular: function (materialId) {
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:outRegularSampleTaskUrl,
                        data:{
                            materialId:materialId,
                            groupId:this.sampleItem.groupId
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess(res.data.data);
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        this.$alertError('连接超时，请刷新重试');
                        console.log(err);
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            outSingular: function (materialId) {
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:outSingularSampleTaskUrl,
                        data:{
                            materialId:materialId,
                            groupId:this.sampleItem.groupId
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess(res.data.data);
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        this.$alertError('连接超时，请刷新重试');
                        console.log(err);
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            outLost:function(materialId){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:outLostSampleTaskUrl,
                        data:{
                            materialId:materialId,
                            groupId:this.sampleItem.groupId
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess(res.data.data);
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        this.$alertError('连接超时，请刷新重试');
                        console.log(err);
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            textToSpeak:function(text){
                let synth = window.speechSynthesis;
                let utterThis = new SpeechSynthesisUtterance(text);
                utterThis.volume = 1;
                utterThis.pitch = 2;
                synth.speak(utterThis);
            }
        }
    }
</script>

<style scoped lang="scss">
    .sample-item{
        .item-operation{
            width:calc(100% - 200px);
        }
        .item-box{
            width:200px;
            margin-left:20px;
        }
    }
</style>