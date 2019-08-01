<template>
    <el-dialog
            title="添加用户"
            :visible.sync="isAdding"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="40%">
        <el-form :inline="true" :model="addInfo" class="add-form">
            <el-form-item label="用户名">
                <el-input v-model.trim="addInfo.uid" placeholder="用户名"></el-input>
            </el-form-item>
            <el-form-item label="用户描述">
                <el-input v-model.trim="addInfo.name" placeholder="用户描述"></el-input>
            </el-form-item>
            <el-form-item label="密码">
                <el-input v-model.trim="addInfo.password" placeholder="密码" type="password"></el-input>
            </el-form-item>
            <el-form-item label="用户类型">
                <el-select v-model.trim="addInfo.type" placeholder="用户类型" style="width:100%" value="">
                    <el-option v-for="item in userTypeList" :value="item.id" :label="item.name" :key="item.id"></el-option>
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
    import {handleUser} from "../../../utils/formValidate";
    import {userAddUrl} from "../../../plugins/globalUrl";
    import {axiosPost} from "../../../utils/fetchData";
    import {errHandler} from "../../../utils/errorHandler";

    export default {
        name: "AddUser",
        props:{
            isAdding:Boolean,
            userTypeList:Array
        },
        data(){
            return{
                addInfo:{
                    uid:'',
                    name:'',
                    password:'',
                    type:''
                },
                isPending: false,
                isCloseOnModal: false,
            }
        },
        methods:{
            cancel:function(){
                this.addInfo.uid  = '';
                this.addInfo.name = '';
                this.addInfo.password = '';
                this.addInfo.type = '';
                this.$emit("update:isAdding",false);
            },
            submit:function(){
                let result = handleUser(this.addInfo);
                if(result !== ''){
                    this.$alertWarning(result);
                    return;
                }
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url: userAddUrl,
                        data:this.addInfo
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess('添加成功');
                            this.cancel();
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            }
        }
    }
</script>

<style scoped lang="scss">
    .add-form{
        .el-form-item{
            width:40%;
        }
    }
</style>