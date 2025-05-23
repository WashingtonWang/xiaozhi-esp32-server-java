<template>
  <a-layout>
    <!-- 面包屑 -->
    <a-layout-header>
      <div style="padding: 10px 24px">
        <a-skeleton :loading="loading" active avatar>
          <!-- 首页信息 -->
          <a-page-header
            :avatar="{
              props: {
                src: userInfo.avatar ? getAvatarUrl(userInfo.avatar) : '',
                class: 'page-header-content-avatar',
              },
              style:
                'display: block; min-height: 72px; min-width: 72px; border-radius: 72px;',
            }"
            class="page-header-content"
          >
            <template slot="subTitle">
              <p class="page-header-content-title">
                {{ timeFix }}, {{ userInfo.name }}, {{ welcome }}
              </p>
              <a-tooltip title="点击翻译" placement="right">
                <p
                  @click="sentenceShow ? (sentenceShow = false) : (sentenceShow = true)"
                  style="cursor: pointer"
                >
                  {{ sentenceShow ? sentence.content : sentence.note }}
                </p>
              </a-tooltip>
            </template>
            <div class="page-head-content-statistic">
              <a-statistic
                title="对话次数"
                :value="userInfo.messageNumber ? userInfo.messageNumber : 0"
                style="padding: 0 25px; padding-left: 0; text-align: right"
              />
              <a-statistic
                title="活跃设备数"
                :value="userInfo.aliveNumber ? userInfo.aliveNumber : 0"
                style="padding: 0 25px; text-align: right"
              />
              <a-statistic
                title="总设备数"
                :value="userInfo.totalDivce ? userInfo.totalDivce : 0"
                style="padding: 0 25px; padding-right: 0; text-align: right"
              />
            </div>
          </a-page-header>
        </a-skeleton>
      </div>
    </a-layout-header>
    <a-layout-content>
      <div class="layout-content-margin">
        <a-row type="flex" :gutter="[20, 20]">
          <a-col
            :xl="{ order: 0, span: 14 }"
            :lg="{ order: 0, span: 12 }"
            :xs="{ order: 1, span: 24 }"
          >
            <a-card title="聊天记录" :bordered="false">
              <a-skeleton :loading="loading" active :paragraph="{ rows: 10 }">
                <a-spin :spinning="!busy" class="infinite-loading">
                  <!-- 无限滚动 -->
                  <a-list rowKey="messageId">
                    <RecycleScroller
                      v-infinite-scroll="infiniteOnLoad"
                      style="height: 450px; overflow-y: scroll"
                      key-field="messageId"
                      :items="listItem"
                      :item-size="120"
                      :infinite-scroll-disabled="busy"
                      :infinite-scroll-distance="10"
                      :infinite-scroll-immediate-check="busy"
                    >
                      <a-list-item key="messageId" slot-scope="{ item }">
                        <a-list-item-meta>
                          <a-avatar slot="avatar" :src="getAvatarUrl(item.avatar)" />
                          <span slot="title">{{ item.deviceName || name }}</span>
                          <template slot="description">
                            <p>{{ item.description }}</p>
                            <span>{{ moment(item.createTime).fromNow() }}</span>
                            <a-divider style="margin: 12px 0" />
                          </template>
                        </a-list-item-meta>
                      </a-list-item>
                    </RecycleScroller>
                  </a-list>
                </a-spin>
              </a-skeleton>
            </a-card>
          </a-col>
          <a-col
            :xl="{ order: 1, span: 10 }"
            :lg="{ order: 1, span: 12 }"
            :xs="{ order: 0, span: 24 }"
          >
            <a-row :gutter="[20, 20]">
              <a-col>
                <a-card title="设备列表" :bordered="false" :loading="loading">
                  <a-table
                    rowKey="deviceId"
                    size="small"
                    :columns="columns"
                    :dataSource="data"
                    :pagination="{ pageSize: 5 }"
                    :loading=userLoading
                  >
                    <div
                      slot="filterDropdown"
                      slot-scope="{
                        setSelectedKeys,
                        selectedKeys,
                        confirm,
                        clearFilters,
                        column,
                      }"
                      style="padding: 8px"
                    >
                      <a-input
                        v-ant-ref="(c) => (searchInput = c)"
                        :placeholder="`搜索 ${column.title}`"
                        :value="selectedKeys[0]"
                        style="width: 188px; margin-bottom: 8px; display: block"
                        @change="
                          (e) => setSelectedKeys(e.target.value ? [e.target.value] : [])
                        "
                        @pressEnter="
                          () => departmentSearch(selectedKeys, confirm, column.dataIndex)
                        "
                      />
                      <a-button
                        type="primary"
                        icon="search"
                        size="small"
                        style="width: 90px; margin-right: 8px"
                        @click="
                          () => departmentSearch(selectedKeys, confirm, column.dataIndex)
                        "
                      >
                        搜索
                      </a-button>
                      <a-button
                        size="small"
                        style="width: 90px"
                        @click="() => reset(clearFilters)"
                      >
                        重置
                      </a-button>
                    </div>
                    <a-icon
                      slot="filterIcon"
                      slot-scope="filtered"
                      type="search"
                      :style="{ color: filtered ? '#108ee9' : undefined }"
                    />
                    <template
                      slot="customRender"
                      slot-scope="text, record, index, column"
                    >
                      <span v-if="searchText && searchedColumn === column.dataIndex">
                        <template
                          v-for="(fragment, i) in text
                            .toString()
                            .split(
                              new RegExp(`(?<=${searchText})|(?=${searchText})`, 'i')
                            )"
                        >
                          <mark
                            v-if="fragment.toLowerCase() === searchText.toLowerCase()"
                            :key="i"
                            class="highlight"
                            >{{ fragment }}</mark
                          >
                          <template v-else>{{ fragment }}</template>
                        </template>
                      </span>
                      <template v-else>
                        {{ text }}
                      </template>
                    </template>
                  </a-table>
                </a-card>
              </a-col>
            </a-row>
          </a-col>
        </a-row>
      </div>
    </a-layout-content>
    <a-back-top />
  </a-layout>
</template>

<script>
import api from "@/services/api";
import axios from "@/services/axios";
import { getResourceUrl } from "@/services/axios";
import mixin from "@/mixins/index";
import Cookies from "js-cookie";
import { jsonp } from 'vue-jsonp';
import { timeFix, welcome } from "@/utils/util";
const RecycleScroller = window["vue-virtual-scroller"].RecycleScroller;
const infiniteScroll = require("@/utils/vue-infinite-scroll.js").infiniteScroll;

export default {
  mixins: [mixin],
  directives: { infiniteScroll },
  components: { RecycleScroller },
  data() {
    return {
      // 欢迎页面
      timeFix: timeFix(),
      welcome: welcome(),
      sentenceShow: true,
      sentence: {
        content: "",
        note: "",
      },
      controlRule: {
        messageDate: "",
        limit: 0,
        active: false,
      },
      userLoading: true,
      searchText: "",
      searchInput: null,
      searchedColumn: "",
      columns: [
        {
          title: "设备名称",
          dataIndex: "deviceName",
          name: "deviceName",
          scopedSlots: {
            filterDropdown: "filterDropdown",
            filterIcon: "filterIcon",
            customRender: "customRender",
          },
          onFilter: (value, record) =>
            record.deviceName.toString().toLowerCase().includes(value.toLowerCase()),
          onFilterDropdownVisibleChange: (visible) => {
            if (visible) {
              setTimeout(() => {
                this.searchInput.focus();
              }, 0);
            }
          },
        },
        {
          title: "对话次数",
          dataIndex: "total",
          key: "total",
          align: "right",
          sorter: (a, b) => a.total - b.total,
        },
        {
          title: "在线状态",
          dataIndex: "state",
          key: "state",
          align: "right",
          sorter: (a, b) => a.status - b.status,
        },
        {
          title: "上次对话时间",
          dataIndex: "updateTime",
          key: "updateTime",
          align: "right",
          sorter: (a, b) => a.updateTime - b.updateTime,
        },
      ],
      data: [],
      // 加载等待
      busy: true,
      // 动态数据
      listItem: [],
      form: this.$form.createForm(this),
      // 动态页数
      page: 1,
      // 已到最后一条数据
      isLastData: false,
      uploadLoading: false,
    };
  },
  beforeMount() {},
  mounted() {
    this.getSentence()
    this.getDate();
    this.infiniteOnLoad();
  },
  computed: {
    userInfo() {
      return this.$store.getters.USER_INFO;
    },
    
  },
  methods: {
    getAvatarUrl(avatar) {
      return getResourceUrl(avatar);
    },
    updateInformation() {
      this.form.validateFields((err, values) => {
        if (!err) {
          values[values.changeItem] = values.content;
          this.update(values);
        }
      });
    },
    getSentence() {
      const day = this.moment().format("YYYY-MM-DD");
      jsonp(`https://sentence.iciba.com/index.php?c=dailysentence&m=getdetail&title=${day}`, {
        param: "callback",
      }).then((res) => {
        this.sentence = res
      }).catch((err) => {
        this.$message.error(err.errmsg);
      })
    },
    reset(clearFilters) {
      clearFilters();
      this.searchText = "";
    },
    /* 查询设备列表 */
    getDate() {
      axios
        .get({
          url: api.device.query,
        })
        .then((res) => {
          this.userLoading = false
          if (res.code === 200) {
            this.data = res.data.list;
          } else {
            this.$message.error(res.message);
          }
        })
        .catch(() => {
          this.userLoading = false
          this.$message.error("服务器维护/重启中,请稍后再试");
        });
    },
    infiniteOnLoad() {
      if (this.isLastData) return;
      this.busy = false;
      const key = "loading";
      axios
        .get({
          url: api.message.query,
          data: {
            start: this.page,
            limit: 10,
          },
        })
        .then((res) => {
          this.busy = true;
          if (res.code === 200) {
            if (res.data.length === 0) {
              this.$message.warning({ content: "已到最后一条数据", key });
              this.isLastData = true;
              return;
            }
            this.page++;
            res.data.forEach((item) => {
              let description;
              switch (item.type) {
                case 1:
                  description = `${item.deviceName} 于 ${item.createTime} 注册`;
                  break;
                case 2:
                  description = `${item.name} 于 ${item.createTime} 修改 ${item.deviceName} 信息`;
                  break;
                case 3:
                  description = `${item.deviceName} 于 ${item.createTime} 报名`;
                  break;
                case 4:
                  description = `${item.name} 于 ${item.createTime} 取消 ${item.deviceName} 报名`;
                  break;
              }
              item.description = description;
            });
            this.listItem = this.listItem.concat(res.data);
          } else {
            this.$message.error({ content: res.message, key });
          }
        })
        .catch(() => {
          this.busy = true;
        })
        .finally(() => {
          this.loading = false
        })
    },
  },
};
</script>

<style lang="scss" scoped>
.ant-layout >>> .ant-layout-header {
  height: 100%;
  line-height: 100%;
  padding: 0;
  background: #fff;
  .ant-page-header {
    padding: 0;
    .ant-page-header-heading {
      width: auto;
      flex: auto;
      display: flex;
    }
    .ant-page-header-content {
      overflow: unset;
    }
  }
  .ant-page-header-heading-sub-title {
    margin-left: 12px;
  }
}
.page-header-content-title {
  margin-bottom: 12px;
  color: rgba(0, 0, 0, 0.85);
  font-weight: 500;
  font-size: 20px;
  line-height: 28px;
}
.page-head-content-statistic {
  display: -webkit-box;
}
.page-header-content {
  display: flex;
}

.infinite-loading {
  // width: 100%;
  // text-align: center;
}

@media screen and (max-width: 800px) {
  .page-head-content-statistic {
    width: max-content;
    margin: 0 auto;
  }
  .page-header-content {
    display: table-cell;
  }
}
.ant-btn-link {
  color: rgba(0, 0, 0, 0.65);
}
.highlight {
  background-color: rgb(255, 192, 105);
  padding: 0px;
}
</style>