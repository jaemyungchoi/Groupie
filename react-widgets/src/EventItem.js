import { Item } from 'semantic-ui-react'
import moment from 'moment';

function EventItem(props) {
    let title = props?.data?.name;
    let description = props?.data?.info;
    let date = props?.data?.dates?.start?.dateTime;
    let url = props?.data?.url;
    let thumbnailUrl = props?.data?.images?.[0]?.url;
    let venue = props?.data?._embedded?.venues?.[0]?.name;
    let city = props?.data?._embedded?.venues?.[0]?.city?.name;
    return (
        <Item>
            <Item.Image size='small'><img src={thumbnailUrl} crossOrigin='anonymous' /></Item.Image>

            <Item.Content>
                <Item.Header as='a' href={url} target='_blank'>{title}</Item.Header>
                <Item.Meta>
                    <span>{moment(date).format('MMM Do YYYY, h:mm a')}</span>
                    {venue && <span>@{venue},</span>}
                    {city && <span>{city}</span>}
                </Item.Meta>
                <Item.Description content={description} />
                <Item.Extra content={props.extraWidget} />
            </Item.Content>
        </Item>
    );
}

export default EventItem;