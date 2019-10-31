<template>
        <el-dialog
                @close="setClose"
                :close-on-click-modal="isCloseOnModal"
                :close-on-press-escape="isCloseOnModal"
                :visible="isPreciousCut"
                width="70%">
            <el-table
                    border
                    :data="cutData"
                    style="width:100%">
                <el-table-column
                        prop="materialId"
                        label="料盘">
                </el-table-column>
                <el-table-column
                        prop="taskLogQuantity"
                        label="出库数量">
                </el-table-column>
                <el-table-column
                        prop="remainderQuantity"
                        label="剩余数量">
                </el-table-column>
                <el-table-column
                        prop="productionTime"
                        label="生产日期">
                </el-table-column>
                <el-table-column
                        label="操作">
                    <template slot-scope="scope">
                        <el-button
                                @click="printBarcode(scope.row)"
                                size="mini"
                                type="primary">打印</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-dialog>
</template>

<script>
    import {mapGetters,mapActions} from 'vuex'
    import {axiosPost} from "../../../../../utils/fetchData";
    import {printUrl} from "../../../../../plugins/globalUrl";
    import {errHandler} from "../../../../../utils/errorHandler";

    export default {
        name: "CutItemDetails",
        data(){
            return{
                isCloseOnModal:false
            }
        },
        props:{
            cutData:Array,
            taskItem:Object,
            isCut:Boolean
        },
        computed:{
            ...mapGetters(['configData','user','isPreciousCut'])
        },
        methods:{
            ...mapActions(['setIsPreciousCut']),
            setClose:function () {
                this.setIsPreciousCut(false);
            },
            printBarcode: function (item) {
                if (this.configData.printerIP === "") {
                    this.$alertWarning("请在设置界面填写打印机IP");
                    return;
                }
                let options = {
                    url: printUrl,
                    data: {
                        ip: this.configData.printerIP,
                        materialId: item.materialId,
                        quantity:item.remainderQuantity
                    }
                };
                axiosPost(options).then(response => {
                    if(response.data.result === 200){
                        this.$alertSuccess('打印成功');
                    }else{
                        errHandler(response.data);
                    }
                }).catch(err => {
                    console.log(err);
                    this.$alertError('连接超时，请刷新重试');
                })
            }
        }
    }
</script>

<style scoped lang="scss">
</style>