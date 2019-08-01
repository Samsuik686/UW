<template>
    <el-dialog
            title="详细"
            :visible.sync="dialogVisible"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="95%">
        <el-form :inline="true">
            <el-form-item label="出入库类型">
                <el-select v-model.trim="type" placeholder="出入库类型" value="">
                    <el-option label="全部" selected="selected"  value='3'></el-option>
                    <el-option label="出库" value='0'></el-option>
                    <el-option label="入库" value='1'></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="发料目的地">
                <el-select v-model.trim="destination" placeholder="发料目的地" value="" :disabled="type === '1' || type === '3'">
                    <el-option label="不限" selected="selected"  value=''></el-option>
                    <el-option  v-for="item in destinations" :label="item.name" :value='item.id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="出入库时间">
                <el-date-picker
                        :clearable="isClear"
                        v-model="times"
                        type="datetimerange"
                        align="right"
                        value-format="yyyy-MM-dd HH:mm:ss"
                        range-separator="-"
                        :default-time="['00:00:00','23:59:59']"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期">
                </el-date-picker>
            </el-form-item>
            <el-form-item style="width:100%;">
                <el-button type="primary" icon="el-icon-search" @click="setFilter">查询</el-button>
                <el-button type="info" icon="el-icon-close" @click="initForm">清除条件</el-button>
            </el-form-item>
        </el-form>
        <el-table
                v-loading="isLoading"
                :data="tableData"
                style="width:100%">
            <el-table-column
                    label="任务名"
                    prop="fileName">
            </el-table-column>
            <el-table-column
                    label="类型"
                    prop="taskType">
            </el-table-column>
            <el-table-column
                    label="计划数量"
                    prop="planQuantity">
            </el-table-column>
            <el-table-column
                    label="实际数量"
                    prop="actualQuantity">
            </el-table-column>
            <el-table-column
                    label="操作员"
                    prop="operator">
            </el-table-column>
            <el-table-column
                    label="出入库时间"
                    prop="ioTime">
            </el-table-column>
        </el-table>
        <div class="block">
            <el-pagination
                    background
                    :current-page.sync="pageNo"
                    :page-size.sync="pageSize"
                    :page-sizes="[20,40,80,100]"
                    @size-change="handlePageSize"
                    @current-change="select"
                    layout="total,sizes,prev,pager,next,jumper"
                    :total="totallyData">
                >
            </el-pagination>
        </div>
    </el-dialog>
</template>

<script>
    import Bus from './../../../../utils/bus'
    import {destinationSelectUrl, getMaterialRecordsUrl, materialEntityUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";
    export default {
        name: "BoxDetails",
        data(){
            return{
                isCloseOnModal:false,
                dialogVisible:false,
                tableData: [],
                pageNo: 1,
                pageSize: 20,
                totallyData:0,
                isPending:false,
                isLoading:false,
                materialTypeId:'',
                type:'3',
                destination:'',
                startTime:'',
                endTime:'',
                times:[],
                destinations:[],
                isClear:false
            }
        },
        created(){
            this.selectDestination();
        },
        beforeDestroy(){
            Bus.$off('showIODetails');
        },
        mounted(){
            Bus.$on('showIODetails',(row) => {
                this.materialTypeId = row.id;
                this.select();
                this.dialogVisible = true;
            })
        },
        methods:{
            select:function () {
                if(this.times !== null){
                    this.startTime = this.times[0];
                    this.endTime = this.times[1];
                }else{
                    this.startTime = '';
                    this.endTime = '';
                }
                if(!this.isPending){
                    this.isPending = true;
                    this.isLoading = true;
                    let options = {
                        url: getMaterialRecordsUrl,
                        data: {
                            materialTypeId:this.materialTypeId,
                            type:this.type,
                            destination:this.destination,
                            startTime:this.startTime,
                            endTime:this.endTime,
                            pageNo: this.pageNo,
                            pageSize: this.pageSize
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            if (res.data.data.list !== null) {
                                this.tableData = res.data.data.list;
                                this.totallyData = res.data.data.totalRow;
                            } else {
                                this.tableData = [];
                                this.totallyData = 0;
                            }
                        }else{
                            errHandler(res.data);
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
            initForm:function(){
                this.startTime = '';
                this.endTime = '';
                this.destination = '';
                this.type = '3';
                this.times = [];
            },
            selectDestination:function(){
                let options = {
                    url: destinationSelectUrl,
                    data: {}
                };
                axiosPost(options).then(res => {
                    if(res.data.result === 200){
                        this.destinations = res.data.data.list;
                    }else{
                        errHandler(res.data);
                    }
                }).catch(err => {
                    console.log(err);
                    this.$alertError('连接超时，请刷新重试');
                })
            },
            setFilter:function(){
                this.pageNo = 1;
                this.pageSize = 20;
                this.select();
            },
            handlePageSize:function(){
                this.pageNo = 1;
                this.select();
            }
        }
    }
</script>

<style scoped lang="scss">
    .block {
        margin-top: 15px;
    }
</style>