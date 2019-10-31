<template>
    <el-dialog
            title="创建任务"
            :visible.sync="isAdding"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="30%">
        <el-form>
            <el-form-item label="客户">
                <el-select v-model.trim="supplierId" placeholder="客户" value="" style="width:100%">
                    <el-option  v-for="item in suppliers" :label="item.name" :value='item.id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="参与盘点仓位">
                <el-select v-model.trim="selectDestinations" placeholder="仓位" value="" style="width:100%" multiple>
                    <el-option  v-for="item in destinations" :label="item.name" :value='item.id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button type="info" size="mini" @click="cancel" >取 消</el-button>
            <el-button type="primary" size="mini" @click="submit">确定</el-button>
        </span>
    </el-dialog>
</template>

<script>
    import {errHandler} from "../../../../utils/errorHandler";
    import {createInventoryRegularTask, destinationSelectUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";

    export default {
        name: "AddTask",
        data(){
            return{
                isPending:false,
                isCloseOnModal:false,
                supplierId:'',
                destinations:[],
                selectDestinations:[]
            }
        },
        props:{
            isAdding:Boolean,
            suppliers:Array,
        },
        mounted(){
            this.getDestinations();
        },
        methods:{
            cancel:function(){
                this.supplierId = '';
                this.selectDestinations = [];
                this.$emit("update:isAdding",false);
            },
            submit:function(){
                if (this.supplierId === '') {
                    this.$alertWarning('客户不能为空');
                    return;
                }
                if (!this.isPending) {
                    this.isPending = true;
                    let options = {
                        url: createInventoryRegularTask,
                        data: {
                            supplierId: this.supplierId,
                            destinationIds:this.selectDestinations.toString()
                        }
                    };
                    axiosPost(options).then(response => {
                        if (response.data.result === 200) {
                            this.$alertSuccess('创建任务成功');
                            this.cancel();
                        } else {
                            errHandler(response.data);
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            getDestinations:function(){
                let options = {
                    url:destinationSelectUrl,
                    data:{}
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
            }
        }
    }
</script>

<style scoped>

</style>