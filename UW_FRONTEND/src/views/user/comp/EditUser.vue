<template>
    <el-dialog
            title="修改用户信息"
            :visible.sync="isEditing"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="40%">
        <el-form :inline="true" :model="editInfo" class="edit-form">
            <el-form-item label="用户名">
                <el-input v-model.trim="editInfo.uid" placeholder="用户名" disabled></el-input>
            </el-form-item>
            <el-form-item label="用户描述">
                <el-input v-model.trim="editInfo.name" placeholder="用户描述"></el-input>
            </el-form-item>
            <el-form-item label="密码">
                <el-input v-model.trim="editInfo.password" placeholder="密码" type="password"></el-input>
            </el-form-item>
            <el-form-item label="用户类型">
                <el-select v-model.trim="editInfo.type" placeholder="用户类型" style="width:100%" value="">
                    <el-option v-for="item in userTypeList" :value="item.id" :label="item.name" :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="是否启用">
                <el-select v-model.trim="editInfo.enabled" placeholder="是否启用" value="">
                    <el-option label="禁用" value=0></el-option>
                    <el-option label="启用" value=1></el-option>
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
    import {userUpdateUrl} from "../../../plugins/globalUrl";
    import {axiosPost} from "../../../utils/fetchData";
    import {errHandler} from "../../../utils/errorHandler";
    import {handleUser} from "../../../utils/formValidate";

    export default {
        name: "EditUser",
        props:{
            isEditing:Boolean,
            editData:Object,
            userTypeList:Array
        },
        data(){
            return{
                editInfo:{
                    uid:'',
                    name:'',
                    password:'',
                    type:'',
                    enabled:''
                },
                isPending: false,
                isCloseOnModal: false,
            }
        },
        methods:{
            clearForm:function(){
                this.editInfo.uid = '';
                this.editInfo.password = '';
                this.editInfo.name = '';
                this.editInfo.type = '';
                this.editInfo.enabled = '';
            },
            cancel:function(){
                this.$emit("update:isEditing",false);
            },
            submit:function(){
                let result = handleUser(this.editInfo);
                if(result !== ''){
                    this.$alertWarning(result);
                    return;
                }
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url: userUpdateUrl,
                        data:this.editInfo
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess('更新成功');
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
        },
        watch:{
            isEditing:function (val) {
                if(val === true){
                    this.editInfo.uid = this.editData.uid;
                    this.editInfo.password = this.editData.password;
                    this.editInfo.name = this.editData.name;
                    this.editInfo.type = this.editData.type;
                    this.editInfo.enabled = this.editData.enabled === true?'1':'0'
                }else{
                    this.clearForm();
                }
            }
        },
    }
</script>

<style scoped lang="scss">
    .edit-form{
        .el-form-item{
            width:40%;
        }
    }
</style>