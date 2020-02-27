<template>
    <el-dialog
            title="创建任务"
            :visible.sync="isAdding"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="30%">
        <el-form>
            <el-form-item label="任务类型">
                <el-select v-model.trim="taskType"
                           placeholder="任务类型" value="" style="width:100%">
                    <el-option  label="入库" value='0'></el-option>
                    <el-option  label="出库" value='1'></el-option>
                    <el-option label="调拨入库" value="4"></el-option>
                    <el-option label="出库-盘点前申补" value="11"></el-option>
                    <el-option label="发料区-紧急出库" value="8"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="客户">
                <el-select v-model.trim="supplier" placeholder="客户" value="" style="width:100%">
                    <el-option  v-for="item in suppliers" :label="item.name" :value='item.id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item :label="destinationTip" v-if="isDestinationShow">
                <el-select v-model.trim="destination" placeholder="仓库" value="" style="width:100%">
                    <el-option  v-for="item in destinations" :label="item.name" :value='item.id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="盘点任务" v-if="isInventoryApply">
                <el-select v-model.trim="inventoryTaskId" placeholder="盘点任务" value="" style="width:100%">
                    <el-option  v-for="item in inventoryTasks" :label="item.file_name" :value='item.task_id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item  label="选择文件">
                <input type="file" style="display:none;" id="fileUpload" @change="handleFileChange"/>
                <el-input id="uploadFile" size="large" @click.native="handleUpload" v-model="fileName"
                          placeholder="请选择"></el-input>
            </el-form-item>
            <el-form-item label="备注">
                <el-input type="textarea" v-model.trim="remarks"></el-input>
            </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button type="info" size="mini" @click="cancel" >取 消</el-button>
            <el-button type="primary" size="mini" @click="submit">确定</el-button>
        </span>
    </el-dialog>
</template>

<script>
    import Bus from '../../../../utils/bus.js'
    import axios from '../../../../plugins/http'
    import {mapGetters} from 'vuex'
    import {
        destinationSelectUrl,
        getUnStartInventoryTaskUrl,
        taskCreateRegularIOTaskUrl,
    } from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";

    export default {
        name: "AddTask",
        data(){
            return{
                isPending:false,
                isCloseOnModal:false,
                taskType:'',
                supplier:'',
                destination:'',
                thisFile:'',
                remarks:'',
                fileName:'',
                destinations:[],
                isInventoryApply:false,
                inventoryTaskId:'',
                inventoryTasks:[],
                isDestinationShow:false,
                destinationTip:'目的地',
                activeCompanyId: parseInt(window.localStorage.getItem('activeCompanyId'))
            }
        },
        computed:{
            ...mapGetters(['token'])
        },
        created(){
            this.selectDestination();
        },
        watch: {
            taskType: function (val) {
                this.isDestinationShow = val === "1" || val === "4" || val === "11" || val === "8";
                this.isInventoryApply = val === "11";
                if(val === '1' || val === "8"){
                    this.destinationTip="目的地"
                }else{
                    this.destinationTip="退料仓位"
                }
            },
            destination:function(val){
                if(val !== ''){
                    if(this.taskType === '11'){
                        this.getUnStartInventoryTasks(val);
                    }
                }
            }
        },
        props:{
            isAdding:Boolean,
            suppliers:Array,
        },
        methods:{
            cancel:function(){
                this.taskType = '';
                this.supplier = '';
                this.inventoryTaskId = '';
                this.destination = '';
                this.remarks = '';
                this.fileName = '';
                this.thisFile = '';
                this.$emit("update:isAdding",false);
            },
            submit:function(){
                if(!this.isPending){
                    if(this.taskType === ''){
                        this.$alertWarning('任务类型不能为空');
                        return;
                    }
                    let formData = new FormData();
                   if(this.supplier === ''){
                       this.$alertWarning('客户不能为空');
                       return;
                   }
                   if(this.taskType !== '0'){
                       if(this.destinations === ''){
                           this.$alertWarning(this.destinationTip + '不能为空');
                           return;
                       }
                       formData.append('destination', this.destination);
                   }
                    if(this.isInventoryApply === true) {
                        if (this.inventoryTaskId === '') {
                            this.$alertWarning("盘点任务不可为空");
                            return;
                        }
                        formData.append('isInventoryApply', this.isInventoryApply);
                        formData.append('inventoryTaskId', this.inventoryTaskId);
                    }
                   if(this.thisFile === ''){
                       this.$alertWarning('任务不能为空');
                       return;
                   }
                   if(this.remarks === ''){
                       this.$alertWarning('备注不能为空');
                       return;
                   }
                    if(this.taskType === '11')this.taskType = 1;
                    formData.append('type', this.taskType);
                    formData.append('file', this.thisFile);
                    formData.append('supplier', this.supplier);
                    formData.append('remarks',this.remarks);
                    formData.append('#TOKEN#', this.token);
                    formData.append('isForced',false);
                    this.isPending = true;
                    axios.post(taskCreateRegularIOTaskUrl, formData).then(res => {
                        if (res.data.result === 200) {
                            this.$alertSuccess('创建成功');
                            this.cancel();
                        }else if(res.data.result === 413){
                            let task = {};
                            if(this.destination)task['destination']=this.destination;
                            if(this.isInventoryApply)task['isInventoryApply']=this.isInventoryApply;
                            if(this.inventoryTaskId)task['inventoryTaskId']=this.inventoryTaskId;
                            if(this.taskType)task['type']=this.taskType;
                            if(this.thisFile)task['file']=this.thisFile;
                            if(this.supplier)task['supplier']=this.supplier;
                            if(this.remarks)task['remarks']=this.remarks;
                            if(this.token)task['#TOKEN#']=this.token;
                            Bus.$emit('setNonexistentMaterial', this.fileName, this.suppliers,task,res.data.data);
                            this.cancel();
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        console.log(err);
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            selectDestination:function(){
                let options = {
                    url: destinationSelectUrl,
                    data: {
                        filter: 'company.id=' + this.activeCompanyId
                    }
                };
                axiosPost(options).then(res => {
                    if(res.data.result === 200){
                        let data = res.data.data.list;
                        let list = [];
                        data.map((item) => {
                            if(item.id !== 0 && item.id !== -1){
                                list.push(item);
                            }
                        });
                        this.destinations = list;
                    }else{
                        errHandler(res.data);
                    }
                }).catch(err => {
                    console.log(err);
                    this.$alertError('连接超时，请刷新重试');
                })
            },
            getUnStartInventoryTasks: function (whId) {
                if(this.supplier === ''){
                    this.$alertWarning('请先选择客户');
                    return;
                }
                if (!this.isPending) {
                    this.isPending = true;
                    let options = {
                        url: getUnStartInventoryTaskUrl,
                        data: {
                            supplierId:this.supplier,
                            warehouseType:0,
                            whId:whId
                        }
                    };
                    axiosPost(options).then(response => {
                        if (response.data.result === 200) {
                            this.inventoryTasks = response.data.data;
                        } else {
                            errHandler(response.data)
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            handleUpload: function () {
                let file = document.getElementById('fileUpload');
                file.value = null;
                file.click();
            },
            handleFileChange: function () {
                let files = document.getElementById('fileUpload');
                let file = files.files[0];
                this.thisFile = file;
                this.fileName = file.name;
            }
        }
    }
</script>

<style scoped>

</style>